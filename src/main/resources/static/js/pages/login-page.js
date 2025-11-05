import { bindBaseUrlInput, onBaseUrlChange, formatUrlForDisplay } from '../common/config.js';
import { telegramLogin, parseTelegramWidgetString, buildTelegramPayloadFromForm, ensureNumber } from '../common/auth.js';
import { refreshAccessToken, logout, getStoredAccessToken, setStoredAccessToken, clearStoredAccessToken, describeTokenState, onAccessTokenChange, getCsrfToken } from '../common/tokens.js';
import { graphqlQuery, formatGraphQlError } from '../common/graphql.js';
import { appendLog, withButtonLoader, updateTextContent, bindCopyToClipboard } from '../common/ui.js';

const elements = {
  baseInput: document.querySelector('[data-el="base-input"]'),
  baseDisplay: document.querySelector('[data-el="base-display"]'),
  loginForm: document.querySelector('[data-el="login-form"]'),
  loginButton: document.querySelector('[data-el="login-button"]'),
  widgetPreview: document.querySelector('[data-el="widget-preview"]'),
  rawTextarea: document.querySelector('[data-el="raw-payload"]'),
  rawButton: document.querySelector('[data-el="login-raw"]'),
  refreshButton: document.querySelector('[data-el="refresh-button"]'),
  logoutButton: document.querySelector('[data-el="logout-button"]'),
  checkAccessButton: document.querySelector('[data-el="check-access"]'),
  manualTokenInput: document.querySelector('[data-el="manual-token"]'),
  saveTokenButton: document.querySelector('[data-el="save-token"]'),
  clearTokenButton: document.querySelector('[data-el="clear-token"]'),
  copyCsrfButton: document.querySelector('[data-el="copy-csrf"]'),
  tokenPreview: document.querySelector('[data-el="token-preview"]'),
  csrfValue: document.querySelector('[data-el="csrf-value"]'),
  log: document.querySelector('[data-el="log"]'),
  status: document.querySelector('[data-el="status"]'),
};

function updateStatus(text, state = 'default') {
  if (!elements.status) return;
  elements.status.textContent = text;
  elements.status.classList.remove('status-pill--ok', 'status-pill--error');
  if (state === 'ok') {
    elements.status.classList.add('status-pill--ok');
  } else if (state === 'error') {
    elements.status.classList.add('status-pill--error');
  }
}

function syncTokenState() {
  const state = describeTokenState();
  updateTextContent(elements.baseDisplay, formatUrlForDisplay(state.baseUrl));
  updateTextContent(elements.tokenPreview, state.accessTokenPreview);
  updateTextContent(elements.csrfValue, state.csrfToken);
  if (elements.manualTokenInput) {
    elements.manualTokenInput.value = getStoredAccessToken();
  }
}

async function handleLogin(payloadProvider, button) {
  return withButtonLoader(button, async () => {
    try {
      const payload = payloadProvider();
      if (!payload) {
        appendLog(elements.log, '❗ Заполните данные для входа');
        return;
      }
      updateStatus('Отправляем данные в Telegram login…');
      const result = await telegramLogin(payload);
      updateStatus('Вход выполнен', 'ok');
      appendLog(elements.log, '✅ telegram-login ok', result.body || '(пустой ответ)');
      syncTokenState();
    } catch (err) {
      console.error(err);
      updateStatus('Ошибка входа', 'error');
      appendLog(elements.log, '❌ telegram-login error', err.status, err.body || err.message);
      throw err;
    }
  });
}

function fillLoginForm(payload) {
  if (!elements.loginForm || !payload) return;
  const mapping = {
    id: 'id',
    firstName: 'firstName',
    lastName: 'lastName',
    username: 'username',
    photoUrl: 'photoUrl',
    authDate: 'authDate',
    hash: 'hash',
  };
  Object.entries(mapping).forEach(([field, name]) => {
    const input = elements.loginForm.elements.namedItem(name);
    if (input) {
      input.value = payload[field] != null ? payload[field] : '';
    }
  });
}

function updateWidgetPreview(payload) {
  if (!elements.widgetPreview) return;
  if (!payload) {
    elements.widgetPreview.textContent = '(нет данных)';
    return;
  }
  const parts = [];
  if (payload.username) parts.push(`@${payload.username}`);
  if (payload.firstName || payload.lastName) {
    parts.push(`${payload.firstName || ''} ${payload.lastName || ''}`.trim());
  }
  if (payload.id) parts.push(`#${payload.id}`);
  if (payload.authDate) {
    const date = new Date(Number(payload.authDate) * 1000);
    if (!Number.isNaN(date.getTime())) {
      parts.push(date.toLocaleString());
    }
  }
  elements.widgetPreview.textContent = parts.filter(Boolean).join(' • ') || '(нет данных)';
}

function convertWidgetUser(user) {
  if (!user) return null;
  return {
    id: ensureNumber(user.id),
    firstName: user.first_name || undefined,
    lastName: user.last_name || undefined,
    username: user.username || undefined,
    photoUrl: user.photo_url || undefined,
    authDate: ensureNumber(user.auth_date),
    hash: user.hash || undefined,
  };
}

function initialiseBaseUrl() {
  bindBaseUrlInput(elements.baseInput, syncTokenState);
  onBaseUrlChange(() => syncTokenState());
  syncTokenState();
}

function setupTelegramWidget() {
  window.__handleTelegramAuth = (user) => {
    const payload = convertWidgetUser(user);
    updateWidgetPreview(payload);
    if (payload) {
      appendLog(elements.log, '🤖 Telegram widget payload', payload);
      fillLoginForm(payload);
      if (elements.rawTextarea) {
        elements.rawTextarea.value = JSON.stringify(payload, null, 2);
      }
      updateStatus('Получены данные от Telegram, выполняем вход…');
      handleLogin(() => payload, null).catch(() => {});
    } else {
      appendLog(elements.log, '⚠️ Telegram widget передал пустые данные');
    }
  };

  if (Array.isArray(window.__pendingTelegramUsers) && window.__pendingTelegramUsers.length) {
    const pending = [...window.__pendingTelegramUsers];
    window.__pendingTelegramUsers.length = 0;
    pending.forEach((user) => window.__handleTelegramAuth(user));
  }
}

function setupLoginForm() {
  if (!elements.loginForm) return;
  elements.loginForm.addEventListener('submit', (event) => {
    event.preventDefault();
    handleLogin(() => buildTelegramPayloadFromForm(elements.loginForm), elements.loginButton).catch(() => {});
  });
}

function setupRawPayloadButton() {
  if (!elements.rawButton) return;
  elements.rawButton.addEventListener('click', () => {
    handleLogin(() => parseTelegramWidgetString(elements.rawTextarea.value), elements.rawButton).catch(() => {});
  });
}

function setupRefreshButton() {
  if (!elements.refreshButton) return;
  elements.refreshButton.addEventListener('click', () => {
    withButtonLoader(elements.refreshButton, async () => {
      const result = await refreshAccessToken();
      if (result.ok) {
        updateStatus('Refresh успешен', 'ok');
      } else {
        updateStatus(`Refresh: HTTP ${result.status}`, 'error');
      }
      appendLog(elements.log, '↺ /auth/refresh', result.status, result.body);
      syncTokenState();
    }).catch(() => {});
  });
}

function setupLogoutButton() {
  if (!elements.logoutButton) return;
  elements.logoutButton.addEventListener('click', () => {
    withButtonLoader(elements.logoutButton, async () => {
      const result = await logout();
      if (result.ok) {
        updateStatus('Сессия завершена', 'ok');
      } else {
        updateStatus(`Logout: HTTP ${result.status}`, 'error');
      }
      appendLog(elements.log, '🚪 /auth/logout', result.status, result.body);
      syncTokenState();
    }).catch(() => {});
  });
}

function setupManualTokenControls() {
  if (!elements.saveTokenButton || !elements.manualTokenInput) return;
  elements.saveTokenButton.addEventListener('click', () => {
    const token = elements.manualTokenInput.value.trim();
    setStoredAccessToken(token);
    updateStatus('accessToken сохранён', 'ok');
    appendLog(elements.log, '💾 access token сохранён вручную');
    syncTokenState();
  });
  if (elements.clearTokenButton) {
    elements.clearTokenButton.addEventListener('click', () => {
      clearStoredAccessToken();
      updateStatus('accessToken очищен', 'default');
      appendLog(elements.log, '🧹 access token очищен');
      syncTokenState();
    });
  }
}

function setupCheckAccessButton() {
  if (!elements.checkAccessButton) return;
  elements.checkAccessButton.addEventListener('click', () => {
    withButtonLoader(elements.checkAccessButton, async () => {
      try {
        const result = await graphqlQuery('{ getPlayer { id username } }');
        updateStatus('Access OK', 'ok');
        appendLog(elements.log, '✅ GraphQL getPlayer', result);
      } catch (err) {
        updateStatus('Access error', 'error');
        appendLog(elements.log, '❌ GraphQL getPlayer', formatGraphQlError(err));
        throw err;
      }
    }).catch(() => {});
  });
}

function setupCopyButtons() {
  bindCopyToClipboard(elements.copyCsrfButton, () => getCsrfToken());
}

function initListeners() {
  onAccessTokenChange(() => syncTokenState());
}

initialiseBaseUrl();
setupLoginForm();
setupRawPayloadButton();
setupRefreshButton();
setupLogoutButton();
setupManualTokenControls();
setupCheckAccessButton();
setupCopyButtons();
initListeners();
setupTelegramWidget();

appendLog(elements.log, '🟣 Страница входа загружена. Укажите BASE URL и выполните авторизацию.');
updateStatus('Ожидается вход');
