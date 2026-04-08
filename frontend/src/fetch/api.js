const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

export async function apiFetchAuthenticated(path, options = {}) {
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


    if (res.status === 401) {
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

export async function apiFetchUnauthenicated(path, options = {}) {
    const url = `${API_BASE_URL}${path}`;
    const res = await fetch(url, {
        ...options,
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {}),
        },
    });

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
  return apiFetchUnauthenicated("/api/users/register", {
    method: "POST",
    body: JSON.stringify(user),
  });
}

export function login(user) {
  return apiFetchUnauthenicated("/api/users/login", {
    method: "POST",
    body: JSON.stringify(user),
  });
}

export function getCurrentUser() {
  return apiFetchAuthenticated("/api/users/findMe", {
    method: "GET",
  });
}

export function updateCurrentUsername(name) {
  return apiFetchAuthenticated("/api/users/me/name", {
    method: "PATCH",
    body: JSON.stringify({ name }),
  });
}

export function getAllPets() {
  return apiFetchAuthenticated("/api/pets/getAll", {
    method: "GET"
  });
}

export function getFilteredPets(filters = {}) {
  const searchParams = new URLSearchParams();

  Object.entries(filters).forEach(([key, value]) => {
    if (value !== null && value !== undefined && value !== "") {
      searchParams.append(key, value);
    }
  });

  const queryString = searchParams.toString();
  const path = queryString
    ? `/api/pets/getFiltered?${queryString}`
    : "/api/pets/getFiltered";

  return apiFetchUnauthenicated(path, {
    method: "GET",
  });
}

export function submitSite(contribution) {
  return apiFetchAuthenticated("/api/contributor/submitSite", {
    method: "POST",
    body: JSON.stringify(contribution),
  });
}

export function getApprovedSites() {
  return apiFetchAuthenticated("/api/admin/getApprovedSites", { 
    method: "GET" 
  });
}

export function getDeniedSites() {
  return apiFetchAuthenticated("/api/admin/getDeniedSites", { 
    method: "GET" 
  });
}

export function getPendingSites() {
  return apiFetchAuthenticated("/api/admin/getPendingSites", { 
        method: "GET" 
  });
}

export function approveSite(id) {
  return apiFetchAuthenticated(`/api/admin/approveSite/${id}`, { 
    method: "PATCH"
  });
}

export function denySite(id) {
  return apiFetchAuthenticated(`/api/admin/denySite/${id}`, { 
    method: "PATCH" 
  });
}
