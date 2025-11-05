import { bindBaseUrlInput, onBaseUrlChange, formatUrlForDisplay } from '../common/config.js';
import { describeTokenState, onAccessTokenChange } from '../common/tokens.js';
import { graphqlQuery, formatGraphQlError, stringifyResult } from '../common/graphql.js';
import { appendLog, withButtonLoader, updateTextContent, fillSelect, formatHouseOption } from '../common/ui.js';

const state = {
  houses: [],
};

const elements = {
  baseInput: document.querySelector('[data-el="base-input"]'),
  baseDisplay: document.querySelector('[data-el="base-display"]'),
  tokenPreview: document.querySelector('[data-el="token-preview"]'),
  csrfValue: document.querySelector('[data-el="csrf-value"]'),
  result: document.querySelector('[data-el="result"]'),
  log: document.querySelector('[data-el="log"]'),
  housesSelect: document.querySelector('[data-el="houses-select"]'),
  loadHousesButton: document.querySelector('[data-el="load-houses"]'),
  getPlayerButton: document.querySelector('[data-el="query-player"]'),
  getPlayerHousesButton: document.querySelector('[data-el="query-player-houses"]'),
  getHouseButton: document.querySelector('[data-el="query-house"]'),
  getHousesCfgButton: document.querySelector('[data-el="query-houses-cfg"]'),
  getLogicCfgButton: document.querySelector('[data-el="query-logic-cfg"]'),
};

function syncSession() {
  const stateSnapshot = describeTokenState();
  updateTextContent(elements.baseDisplay, formatUrlForDisplay(stateSnapshot.baseUrl));
  updateTextContent(elements.tokenPreview, stateSnapshot.accessTokenPreview);
  updateTextContent(elements.csrfValue, stateSnapshot.csrfToken);
}

async function executeQuery(button, title, query, variables = {}) {
  return withButtonLoader(button, async () => {
    try {
      const payload = await graphqlQuery(query, variables);
      appendLog(elements.log, `✅ ${title}`, payload);
      updateTextContent(elements.result, stringifyResult(payload));
      return payload;
    } catch (err) {
      const message = formatGraphQlError(err);
      appendLog(elements.log, `❌ ${title}`, message);
      updateTextContent(elements.result, message);
      throw err;
    }
  });
}

async function loadHouses(button) {
  const response = await executeQuery(
    button,
    'Query getPlayerHouses',
    '{ getPlayerHouses { id type level locationX locationY skin } }',
  );
  if (response?.data?.getPlayerHouses) {
    state.houses = response.data.getPlayerHouses;
    fillSelect(
      elements.housesSelect,
      state.houses.map((house) => ({ value: house.id, label: formatHouseOption(house) })),
      '(дома не найдены)',
    );
  }
}

function setupHandlers() {
  if (elements.loadHousesButton) {
    elements.loadHousesButton.addEventListener('click', () => {
      loadHouses(elements.loadHousesButton).catch(() => {});
    });
  }
  if (elements.getPlayerButton) {
    elements.getPlayerButton.addEventListener('click', () =>
      executeQuery(elements.getPlayerButton, 'Query getPlayer', '{ getPlayer { id username photoUrl meat gold brain houses { id type level } } }').catch(() => {}),
    );
  }
  if (elements.getPlayerHousesButton) {
    elements.getPlayerHousesButton.addEventListener('click', () => {
      loadHouses(elements.getPlayerHousesButton).catch(() => {});
    });
  }
  if (elements.getHouseButton) {
    elements.getHouseButton.addEventListener('click', () => {
      const houseId = elements.housesSelect?.value;
      if (!houseId) {
        appendLog(elements.log, '⚠️ Выберите дом из списка');
        return;
      }
      return executeQuery(
        elements.getHouseButton,
        'Query getHouse',
        'query($id: ID!) { getHouse(houseId: $id) { id type level locationX locationY skin telegramId } }',
        { id: houseId },
      ).catch(() => {});
    });
  }
  if (elements.getHousesCfgButton) {
    elements.getHousesCfgButton.addEventListener('click', () =>
      executeQuery(elements.getHousesCfgButton, 'Query getHousesInfoCfg', '{ getHousesInfoCfg }').catch(() => {}),
    );
  }
  if (elements.getLogicCfgButton) {
    elements.getLogicCfgButton.addEventListener('click', () =>
      executeQuery(elements.getLogicCfgButton, 'Query getGameLogicCfg', '{ getGameLogicCfg }').catch(() => {}),
    );
  }
}

function init() {
  bindBaseUrlInput(elements.baseInput, syncSession);
  onBaseUrlChange(syncSession);
  onAccessTokenChange(syncSession);
  syncSession();
  setupHandlers();
  appendLog(elements.log, '🟢 Страница защищённых запросов готова. Все запросы выполняются методом GET.');
}

init();
