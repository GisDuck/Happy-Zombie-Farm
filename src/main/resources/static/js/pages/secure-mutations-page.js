import { bindBaseUrlInput, onBaseUrlChange, formatUrlForDisplay } from '../common/config.js';
import { describeTokenState, onAccessTokenChange } from '../common/tokens.js';
import { graphqlQuery, graphqlMutation, formatGraphQlError, stringifyResult, generateIdempotencyKey } from '../common/graphql.js';
import { appendLog, withButtonLoader, updateTextContent, fillSelect, formatHouseOption } from '../common/ui.js';

const state = {
  houses: [],
};

const elements = {
  baseInput: document.querySelector('[data-el="base-input"]'),
  baseDisplay: document.querySelector('[data-el="base-display"]'),
  tokenPreview: document.querySelector('[data-el="token-preview"]'),
  csrfValue: document.querySelector('[data-el="csrf-value"]'),
  idemInput: document.querySelector('[data-el="idem-key"]'),
  idemGenerate: document.querySelector('[data-el="idem-generate"]'),
  result: document.querySelector('[data-el="result"]'),
  log: document.querySelector('[data-el="log"]'),
  housesSelect: document.querySelector('[data-el="houses-select"]'),
  loadHousesButton: document.querySelector('[data-el="load-houses"]'),
  buildButton: document.querySelector('[data-el="mutation-build"]'),
  buildType: document.querySelector('[data-el="build-type"]'),
  buildSkin: document.querySelector('[data-el="build-skin"]'),
  buildX: document.querySelector('[data-el="build-x"]'),
  buildY: document.querySelector('[data-el="build-y"]'),
  moveButton: document.querySelector('[data-el="mutation-move"]'),
  moveX: document.querySelector('[data-el="move-x"]'),
  moveY: document.querySelector('[data-el="move-y"]'),
  skinButton: document.querySelector('[data-el="mutation-skin"]'),
  skinNew: document.querySelector('[data-el="skin-new"]'),
  levelButton: document.querySelector('[data-el="mutation-level"]'),
  deleteButton: document.querySelector('[data-el="mutation-delete"]'),
  meatButton: document.querySelector('[data-el="mutation-meat"]'),
  convertMeatButton: document.querySelector('[data-el="mutation-convert-meat"]'),
  convertMeatValue: document.querySelector('[data-el="convert-meat"]'),
  convertBrainButton: document.querySelector('[data-el="mutation-convert-brain"]'),
  convertBrainValue: document.querySelector('[data-el="convert-brain"]'),
};

function syncSession() {
  const snapshot = describeTokenState();
  updateTextContent(elements.baseDisplay, formatUrlForDisplay(snapshot.baseUrl));
  updateTextContent(elements.tokenPreview, snapshot.accessTokenPreview);
  updateTextContent(elements.csrfValue, snapshot.csrfToken);
}

function ensureIdempotencyKey() {
  let key = elements.idemInput?.value?.trim();
  if (!key) {
    key = generateIdempotencyKey();
    if (elements.idemInput) {
      elements.idemInput.value = key;
    }
    appendLog(elements.log, '🔑 Сгенерирован новый Idempotency-Key', key);
  }
  return key;
}

async function executeMutation(button, title, query, variables = {}) {
  const key = ensureIdempotencyKey();
  return withButtonLoader(button, async () => {
    try {
      const payload = await graphqlMutation(query, variables, { idempotencyKey: key });
      appendLog(elements.log, `✅ ${title}`, { key, payload });
      updateTextContent(elements.result, stringifyResult(payload));
      return payload;
    } catch (err) {
      const message = formatGraphQlError(err);
      appendLog(elements.log, `❌ ${title}`, { key, message });
      updateTextContent(elements.result, message);
      throw err;
    }
  });
}

async function loadHouses(button) {
  const response = await withButtonLoader(button, async () =>
    graphqlQuery('{ getPlayerHouses { id type level locationX locationY skin } }'),
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

function requireHouseId() {
  const houseId = elements.housesSelect?.value;
  if (!houseId) {
    appendLog(elements.log, '⚠️ Сначала обновите список и выберите дом');
    throw new Error('house not selected');
  }
  return houseId;
}

function setupIdempotencyControls() {
  if (elements.idemGenerate) {
    elements.idemGenerate.addEventListener('click', () => {
      const nextKey = generateIdempotencyKey();
      if (elements.idemInput) {
        elements.idemInput.value = nextKey;
      }
      appendLog(elements.log, '🔄 Сгенерирован новый Idempotency-Key вручную', nextKey);
    });
  }
}

function setupHouseLoader() {
  if (elements.loadHousesButton) {
    elements.loadHousesButton.addEventListener('click', () => {
      loadHouses(elements.loadHousesButton).catch(() => {});
    });
  }
}

function setupMutations() {
  if (elements.buildButton) {
    elements.buildButton.addEventListener('click', () => {
      const type = elements.buildType?.value || 'FARM';
      const skin = elements.buildSkin?.value?.trim() || 'default';
      const locationX = Number(elements.buildX?.value || 0);
      const locationY = Number(elements.buildY?.value || 0);
      return executeMutation(
        elements.buildButton,
        'Mutation buildHouse',
        'mutation($input: BuildHouseInput!) { buildHouse(input: $input) { id type level skin locationX locationY } }',
        { input: { type, skin, locationX, locationY } },
      ).catch(() => {});
    });
  }

  if (elements.moveButton) {
    elements.moveButton.addEventListener('click', () => {
      const houseId = requireHouseId();
      const newLocationX = Number(elements.moveX?.value || 0);
      const newLocationY = Number(elements.moveY?.value || 0);
      return executeMutation(
        elements.moveButton,
        'Mutation updateHouseLocation',
        'mutation($input: UpdateHouseLocationInput!) { updateHouseLocation(input: $input) { id locationX locationY } }',
        { input: { houseId, newLocationX, newLocationY } },
      ).catch(() => {});
    });
  }

  if (elements.skinButton) {
    elements.skinButton.addEventListener('click', () => {
      const houseId = requireHouseId();
      const newSkin = elements.skinNew?.value?.trim() || 'default';
      return executeMutation(
        elements.skinButton,
        'Mutation updateHouseSkin',
        'mutation($input: UpdateHouseSkinInput!) { updateHouseSkin(input: $input) { id skin } }',
        { input: { houseId, newSkin } },
      ).catch(() => {});
    });
  }

  if (elements.levelButton) {
    elements.levelButton.addEventListener('click', () => {
      const houseId = requireHouseId();
      return executeMutation(
        elements.levelButton,
        'Mutation updateHouseLevel',
        'mutation($input: HouseIdInput!) { updateHouseLevel(input: $input) { id level } }',
        { input: { houseId } },
      ).catch(() => {});
    });
  }

  if (elements.deleteButton) {
    elements.deleteButton.addEventListener('click', () => {
      const houseId = requireHouseId();
      return executeMutation(
        elements.deleteButton,
        'Mutation removeHouse',
        'mutation($input: HouseIdInput!) { removeHouse(input: $input) { success deletedHouseId } }',
        { input: { houseId } },
      ).catch(() => {});
    });
  }

  if (elements.meatButton) {
    elements.meatButton.addEventListener('click', () =>
      executeMutation(
        elements.meatButton,
        'Mutation updatePlayerMeat',
        'mutation { updatePlayerMeat { id meat gold brain } }',
      ).catch(() => {}),
    );
  }

  if (elements.convertMeatButton) {
    elements.convertMeatButton.addEventListener('click', () => {
      const meatToSpend = Number(elements.convertMeatValue?.value || 0);
      return executeMutation(
        elements.convertMeatButton,
        'Mutation convertMeatToBrain',
        'mutation($input: ConvertMeatToBrainInput!) { convertMeatToBrain(input: $input) { id meat brain gold } }',
        { input: { meatToSpend } },
      ).catch(() => {});
    });
  }

  if (elements.convertBrainButton) {
    elements.convertBrainButton.addEventListener('click', () => {
      const brainToSpend = Number(elements.convertBrainValue?.value || 0);
      return executeMutation(
        elements.convertBrainButton,
        'Mutation convertBrainToGold',
        'mutation($input: ConvertBrainToGoldInput!) { convertBrainToGold(input: $input) { id brain gold meat } }',
        { input: { brainToSpend } },
      ).catch(() => {});
    });
  }
}

function init() {
  bindBaseUrlInput(elements.baseInput, syncSession);
  onBaseUrlChange(syncSession);
  onAccessTokenChange(syncSession);
  syncSession();
  setupIdempotencyControls();
  setupHouseLoader();
  setupMutations();
  appendLog(elements.log, '🛡️ Страница мутаций с Idempotency-Key готова. Для каждой операции используется текущий ключ.');
  ensureIdempotencyKey();
}

init();
