import { getBaseUrl, makeAbsoluteUrl } from './config.js';

const ACCESS_STORAGE_KEY = 'hzf_access_token';
const accessListeners = new Set();

export function getStoredAccessToken() {
  return localStorage.getItem(ACCESS_STORAGE_KEY) || '';
}

export function setStoredAccessToken(token) {
  if (token) {
    localStorage.setItem(ACCESS_STORAGE_KEY, token);
  } else {
    localStorage.removeItem(ACCESS_STORAGE_KEY);
  }
  accessListeners.forEach((cb) => {
    try {
      cb(getStoredAccessToken());
    } catch (err) {
      console.error('access token listener error', err);
    }
  });
}

export function clearStoredAccessToken() {
  setStoredAccessToken('');
}

export function onAccessTokenChange(callback) {
  accessListeners.add(callback);
  return () => accessListeners.delete(callback);
}

export function readCookie(name) {
  if (typeof document === 'undefined') return '';
  const pair = document.cookie
    .split(';')
    .map((c) => c.trim())
    .find((c) => c.startsWith(`${name}=`));
  if (!pair) return '';
  return decodeURIComponent(pair.split('=')[1] || '');
}

export function getCsrfToken() {
  return readCookie('XSRF-TOKEN');
}

function tryParseJson(text) {
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch (err) {
    return null;
  }
}

async function parseResponseBody(response) {
  const contentType = response.headers.get('content-type') || '';
  if (contentType.includes('application/json')) {
    try {
      return await response.json();
    } catch (err) {
      return null;
    }
  }
  const text = await response.text();
  return tryParseJson(text) ?? text;
}

export async function refreshAccessToken() {
  const url = makeAbsoluteUrl('/auth/refresh');
  const headers = {
    'Accept': 'application/json, text/plain, */*',
  };
  const csrf = getCsrfToken();
  if (csrf) {
    headers['X-XSRF-TOKEN'] = csrf;
  }
  const response = await fetch(url, {
    method: 'POST',
    credentials: 'include',
    headers,
  });
  const body = await parseResponseBody(response);
  if (body && body.accessToken) {
    setStoredAccessToken(body.accessToken);
  }
  return { ok: response.ok, status: response.status, body };
}

export async function logout() {
  const url = makeAbsoluteUrl('/auth/logout');
  const headers = {
    'Accept': 'application/json, text/plain, */*',
  };
  const csrf = getCsrfToken();
  if (csrf) {
    headers['X-XSRF-TOKEN'] = csrf;
  }
  const response = await fetch(url, {
    method: 'POST',
    credentials: 'include',
    headers,
  });
  if (response.ok) {
    clearStoredAccessToken();
  }
  const body = await parseResponseBody(response);
  return { ok: response.ok, status: response.status, body };
}

export function buildAuthHeaders() {
  const token = getStoredAccessToken();
  if (!token) return {};
  return { Authorization: `Bearer ${token}` };
}

export function describeTokenState() {
  const access = getStoredAccessToken();
  const csrf = getCsrfToken();
  return {
    baseUrl: getBaseUrl(),
    accessTokenPreview: access ? `${access.slice(0, 16)}…${access.slice(-8)}` : '(нет)',
    hasAccessToken: Boolean(access),
    csrfToken: csrf || '(нет)',
  };
}
