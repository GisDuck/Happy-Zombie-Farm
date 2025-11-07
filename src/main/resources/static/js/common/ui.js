export function setButtonLoading(button, isLoading) {
  if (!button) return;
  const originalText = button.dataset.originalText || button.textContent;
  if (!button.dataset.originalText) {
    button.dataset.originalText = originalText;
  }
  if (isLoading) {
    button.classList.add('loading');
    button.disabled = true;
    button.textContent = button.dataset.loadingText || originalText;
  } else {
    button.classList.remove('loading');
    button.disabled = false;
    button.textContent = button.dataset.originalText || originalText;
  }
}

export async function withButtonLoader(button, action) {
  setButtonLoading(button, true);
  try {
    return await action();
  } finally {
    setButtonLoading(button, false);
  }
}

export function appendLog(logElement, ...items) {
  if (!logElement) return;
  const lines = items.map((item) => {
    if (item == null) return 'null';
    if (typeof item === 'string') return item;
    try {
      return JSON.stringify(item, null, 2);
    } catch (err) {
      return String(item);
    }
  });
  const timestamp = new Date().toISOString();
  const line = `[${timestamp}] ${lines.join(' ')}`;
  logElement.textContent = `${line}\n${logElement.textContent}`;
}

export function bindCopyToClipboard(button, valueProvider) {
  if (!button) return () => {};
  const handler = async () => {
    try {
      const value = typeof valueProvider === 'function' ? valueProvider() : valueProvider;
      if (!value) return;
      await navigator.clipboard.writeText(value);
      const original = button.textContent;
      button.textContent = 'Скопировано';
      setTimeout(() => {
        button.textContent = original;
      }, 1500);
    } catch (err) {
      console.error('Не удалось скопировать', err);
    }
  };
  button.addEventListener('click', handler);
  return () => button.removeEventListener('click', handler);
}

export function updateTextContent(element, text) {
  if (!element) return;
  element.textContent = text;
}

export function fillSelect(select, options, placeholder = '(нет данных)') {
  if (!select) return;
  select.innerHTML = '';
  if (!options || !options.length) {
    const opt = document.createElement('option');
    opt.value = '';
    opt.textContent = placeholder;
    select.appendChild(opt);
    return;
  }
  options.forEach((item) => {
    const opt = document.createElement('option');
    opt.value = item.value;
    opt.textContent = item.label;
    select.appendChild(opt);
  });
}

export function formatHouseOption(house) {
  if (!house) return '';
  return `#${house.id} • ${house.type} • lvl ${house.level} • (${house.locationX},${house.locationY}) • skin=${house.skin}`;
}
