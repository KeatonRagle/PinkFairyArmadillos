const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

export async function apiFetch(path, options = {}) {
    const url = `${API_BASE_URL}${path}`;
    const token = localStorage.getItem("token");
    const res = await fetch(url, {
        ...options,
        headers: {
            "Content-Type": "application/json",
            ...(token && { Authorization: `Bearer ${token}` }),
            ...(options.headers || {}),
        },
    });

    if (!res.ok) {
      let message = "Something went wrong";

      try {
        const data = await res.json();
        message = data.message || data.error || message;
      } catch {
        const text = await res.text();
        if (text) message = text;
      }

      throw new Error(message);
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

export function getAllPets() {
  return apiFetch("/api/pets/getAll", {
    method: "GET"
  });
}
