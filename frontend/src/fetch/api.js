const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

export async function apiFetch(path, options = {}) {
    const url = `${API_BASE_URL}${path}`;
    const res = await fetch(url, {
        ...options,
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {}),
        },
    });

    // optional: nicer error handling
    if (!res.ok) {
        const text = await res.text();
        throw new Error(`${res.status} ${res.statusText}: ${text}`);
    }

    // if empty response body (204), avoid json() crash
    const contentType = res.headers.get("content-type") || "";
    return contentType.includes("application/json") ? res.json() : res.text();
}

export function registerUser(user) {
  return apiFetch("/api/users/register", {
    method: "POST",
    body: JSON.stringify(user),
  });
}

export function login(user) {
  return apiFetch("/api/users/login", {
    method: "POST",
    body: JSON.stringify(user),
  });
}
