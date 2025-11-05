import { makeAbsoluteUrl } from './config.js';
import { buildAuthHeaders, getCsrfToken } from './tokens.js';

function mergeHeaders(defaults, custom = {}) {
  const headers = new Headers();
  Object.entries(defaults).forEach(([key, value]) => {
    if (value != null && value !== '') {
      headers.set(key, value);
    }
  });
  Object.entries(custom).forEach(([key, value]) => {
    if (value != null && value !== '') {
      headers.set(key, value);
    }
  });
  return headers;
}

async function parseGraphQlResponse(response) {
  const contentType = response.headers.get('content-type') || '';
  let payload = null;
  if (contentType.includes('application/json')) {
    try {
      payload = await response.json();
    } catch (err) {
      payload = null;
    }
  } else {
    const text = await response.text();
    try {
      payload = JSON.parse(text);
    } catch (err) {
      payload = text;
    }
  }

  if (!response.ok) {
    const error = new Error('GraphQL request failed');
    error.status = response.status;
    error.payload = payload;
    throw error;
  }

  if (payload && payload.errors) {
    const error = new Error('GraphQL responded with errors');
    error.status = response.status;
    error.payload = payload;
    throw error;
  }

  return payload;
}

export async function graphqlQuery(query, variables = {}, options = {}) {
  const url = new URL(makeAbsoluteUrl('/graphql'));
  url.searchParams.set('query', query);
  if (variables && Object.keys(variables).length) {
    url.searchParams.set('variables', JSON.stringify(variables));
  }

  const headers = mergeHeaders(
    {
      'Accept': 'application/json',
      ...buildAuthHeaders(),
    },
    options.headers,
  );

  const response = await fetch(url.toString(), {
    method: 'GET',
    credentials: 'include',
    headers,
    signal: options.signal,
  });

  return parseGraphQlResponse(response);
}

export async function graphqlMutation(query, variables = {}, options = {}) {
  const csrf = getCsrfToken();
  const headers = mergeHeaders(
    {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      ...buildAuthHeaders(),
      ...(csrf ? { 'X-XSRF-TOKEN': csrf } : {}),
      ...(options.idempotencyKey ? { 'Idempotency-Key': options.idempotencyKey } : {}),
    },
    options.headers,
  );

  const response = await fetch(makeAbsoluteUrl('/graphql'), {
    method: 'POST',
    credentials: 'include',
    headers,
    body: JSON.stringify({
      query,
      variables,
    }),
    signal: options.signal,
  });

  return parseGraphQlResponse(response);
}

export function generateIdempotencyKey() {
  if (window.crypto?.randomUUID) {
    return window.crypto.randomUUID();
  }
  const random = Math.random().toString(16).slice(2, 10);
  const time = Date.now().toString(16);
  return `${time}-${random}`;
}

export function stringifyResult(payload) {
  if (!payload) return '∅';
  if (typeof payload === 'string') return payload;
  try {
    return JSON.stringify(payload, null, 2);
  } catch (err) {
    return String(payload);
  }
}

export async function safeGraphQl(action, onError) {
  try {
    return await action();
  } catch (err) {
    if (typeof onError === 'function') {
      onError(err);
    }
    throw err;
  }
}

export function formatGraphQlError(error) {
  if (!error) return 'Неизвестная ошибка';
  const status = error.status ? `HTTP ${error.status}` : 'без HTTP статуса';
  if (error.payload?.errors) {
    return `${status}: ${error.payload.errors.map((e) => e.message).join('; ')}`;
  }
  if (typeof error.payload === 'string') {
    return `${status}: ${error.payload}`;
  }
  if (error.payload) {
    try {
      return `${status}: ${JSON.stringify(error.payload)}`;
    } catch (err) {
      return `${status}`;
    }
  }
  return status;
}
