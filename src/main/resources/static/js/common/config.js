const BASE_URL_STORAGE_KEY = 'hzf_base_url';
const listeners = new Set();

function normaliseBaseUrl(value) {
  if (!value) {
    return window.location.origin;
  }
  try {
    const url = new URL(value, window.location.origin);
    return url.origin;
  } catch (err) {
    return window.location.origin;
  }
}

export function getBaseUrl() {
  const stored = localStorage.getItem(BASE_URL_STORAGE_KEY);
  return stored ? normaliseBaseUrl(stored) : window.location.origin;
}

export function setBaseUrl(value) {
  const next = normaliseBaseUrl(value);
  localStorage.setItem(BASE_URL_STORAGE_KEY, next);
  listeners.forEach((cb) => {
    try {
      cb(next);
    } catch (err) {
      console.error('BASE URL listener error', err);
    }
  });
  return next;
}

export function onBaseUrlChange(callback) {
  listeners.add(callback);
  return () => listeners.delete(callback);
}

export function bindBaseUrlInput(inputEl, onChange) {
  if (!inputEl) return () => {};
  inputEl.value = getBaseUrl();
  const handler = (event) => {
    const value = event.target.value.trim();
    const next = setBaseUrl(value);
    event.target.value = next;
    if (typeof onChange === 'function') {
      onChange(next);
    }
  };
  inputEl.addEventListener('change', handler);
  inputEl.addEventListener('blur', handler);
  return () => {
    inputEl.removeEventListener('change', handler);
    inputEl.removeEventListener('blur', handler);
  };
}

export function makeAbsoluteUrl(path) {
  const base = getBaseUrl();
  return new URL(path, base).toString();
}

export function formatUrlForDisplay(url) {
  try {
    const parsed = new URL(url);
    return `${parsed.protocol}//${parsed.host}`;
  } catch (err) {
    return url;
  }
}
