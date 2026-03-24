const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;
const AUTH_PATHS = ["/api/users/login", "/api/users/register"];

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

    if (res.status === 401 && !AUTH_PATHS.includes(path)) {
        localStorage.removeItem("token");
        localStorage.removeItem("username");
        window.location.href = "/login";
        return;
    }

    if (!res.ok) {
        const err = new Error(`Request failed`);
        err.status = res.status;
        throw err;
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
