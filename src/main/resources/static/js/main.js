function getCookie(name) {
        const cookies = document.cookie ? document.cookie.split("; ") : [];
        for (const cookie of cookies) {
            const [key, ...rest] = cookie.split("=");
            if (key === name) {
                return decodeURIComponent(rest.join("="));
            }
        }
        return null;
    }

function getCsrfToken() {
    return getCookie("XSRF-TOKEN");
}

function generateIdempotencyKey() {
        if (window.crypto && window.crypto.randomUUID) {
            return window.crypto.randomUUID();
        }
        return "idem-" + Date.now() + "-" + Math.random().toString(16).slice(2);
    }

async function callGraphQL(query, variables, description) {
    const key = generateIdempotencyKey();
    const csrf = getCsrfToken();

    const headers = {
        "Content-Type": "application/json",
        "Idempotency-Key": key,
        "X-XSRF-TOKEN": csrf
    };

    const body = JSON.stringify({
        query: query,
        variables: variables || {}
    });

    const meta = {
        description: description || "",
        idempotencyKey: key,
        hasCsrf: true,
        body: JSON.parse(body)
    };

    try {
        const resp = await fetch("/graphql", {
            method: "POST",
            headers: headers,
            credentials: "include",
            body: body
        });

        const text = await resp.text();
        let json;
        try {
            json = JSON.parse(text);
        } catch (e) {
            json = null;
        }

        return {
            ok: resp.ok,
            status: resp.status,
            statusText: resp.statusText,
            json: json,
            rawText: text,
            meta: meta
        };
    } catch (e) {
        return {
            ok: false,
            status: 0,
            statusText: "NETWORK_ERROR",
            json: null,
            rawText: String(e),
            meta: meta
        };
    }
}


async function tryRefresh() {
  const res = await fetch('/auth/refresh', {
    method: 'POST',
    credentials: 'include'
  });

  if (res.ok) return true;
  return false;
}

// Вызывается всегда
(async () => {
  const params = new URLSearchParams(window.location.search);
  const redirectTo = params.get('redirect');

  const ok = await tryRefresh();  // POST /auth/refresh

  if (!redirectTo) {
    // обычный заход на страницу
    return;
  }

  if (ok) {
    window.location.href = redirectTo; // вернуться на защищённую страницу
  }
})();
