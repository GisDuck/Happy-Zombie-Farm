import { makeAbsoluteUrl } from './config.js';
import { setStoredAccessToken } from './tokens.js';

function normaliseAuthPayload(payload) {
  if (!payload || typeof payload !== 'object') return null;
  const allowed = [
    'id',
    'firstName',
    'lastName',
    'username',
    'photoUrl',
    'authDate',
    'hash',
  ];
  const result = {};
  allowed.forEach((key) => {
    if (payload[key] != null) {
      result[key] = payload[key];
    }
  });
  return result;
}

export function parseTelegramWidgetString(raw) {
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch (err) {
    const result = {};
    raw.split('&').forEach((pair) => {
      const [k, v] = pair.split('=');
      if (k) {
        const key = decodeURIComponent(k);
        const value = decodeURIComponent(v || '');
        if (key === 'id' || key === 'auth_date') {
          result[key === 'id' ? 'id' : 'authDate'] = Number(value);
        } else {
          result[key] = value;
        }
      }
    });
    return result;
  }
}

async function requestWithPayload(url, payload) {
  const response = await fetch(url, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json, text/plain, */*',
    },
    body: JSON.stringify(payload),
  });
  const text = await response.text();
  let body = null;
  if (text) {
    try {
      body = JSON.parse(text);
    } catch (err) {
      body = text;
    }
  }
  if (!response.ok) {
    const error = new Error('Auth request failed');
    error.status = response.status;
    error.body = body;
    throw error;
  }
  if (body && body.accessToken) {
    setStoredAccessToken(body.accessToken);
  }
  return { status: response.status, body };
}

export async function telegramLogin(payload) {
  const normalised = normaliseAuthPayload(payload);
  if (!normalised || !normalised.id || !normalised.hash) {
    throw new Error('Неверный payload: требуется id и hash');
  }
  const url = makeAbsoluteUrl('/auth/telegram-login');
  return requestWithPayload(url, normalised);
}

export function ensureNumber(value) {
  if (value == null || value === '') return undefined;
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : undefined;
}

export function buildTelegramPayloadFromForm(form) {
  const data = new FormData(form);
  return {
    id: ensureNumber(data.get('id')),
    firstName: data.get('firstName') || undefined,
    lastName: data.get('lastName') || undefined,
    username: data.get('username') || undefined,
    photoUrl: data.get('photoUrl') || undefined,
    authDate: ensureNumber(data.get('authDate')),
    hash: data.get('hash') || undefined,
  };
}
