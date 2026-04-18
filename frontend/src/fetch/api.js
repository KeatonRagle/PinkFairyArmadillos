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
        headers: { "Content-Type": "application/json",
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
    } });

  const queryString = searchParams.toString();
  const path = queryString
    ? `/api/pets/getFiltered?${queryString}`
    : "/api/pets/getFiltered";

  return apiFetchUnauthenicated(path, {
    method: "GET",
  });
}

export function getFeaturedPets() {
  return apiFetchUnauthenicated("/api/featuredPets/getAll", {
    method: "GET",
  });
}

export function submitSite(contribution) {
  return apiFetchAuthenticated("/api/adoptionSite/submitSite", {
    method: "POST",
    body: JSON.stringify(contribution),
  });
}

export function getApprovedSites() {
  return apiFetchAuthenticated("/api/adoptionSite/getApprovedSites", { 
    method: "GET" 
  });
}

export function getDeniedSites() {
  return apiFetchAuthenticated("/api/adoptionSite/getDeniedSites", { 
    method: "GET" 
  });
}

export function getPendingSites() {
  return apiFetchAuthenticated("/api/adoptionSite/getPendingSites", { 
        method: "GET" 
  });
}

export function approveSite(id) {
  return apiFetchAuthenticated(`/api/adoptionSite/approveSite/${id}`, { 
    method: "PATCH"
  });
}

export function denySite(id) {
  return apiFetchAuthenticated(`/api/adoptionSite/denySite/${id}`, { 
    method: "PATCH" 
  });
}

export function submitPost(post) {
  return apiFetchAuthenticated("/api/posts/submitPost", {
    method: "POST",
    body: JSON.stringify(post),
  });
}

export function deletePost(id) {
  return apiFetchAuthenticated(`/api/posts/${id}`, {
    method: "DELETE",
  });
}

export function getAllPosts() {
  return apiFetchAuthenticated("/api/posts/getAll", {
    method: "GET"
  });
}

export function submitComment(comment) {
  return apiFetchAuthenticated("/api/comments/submitComment", {
    method: "POST",
    body: JSON.stringify(comment),
  });
}

export function getCommentsByPost(postId) {
  return apiFetchAuthenticated(`/api/comments/getByPost/${postId}`, {
    method: "GET"
  });
}

export function deleteComment(id) {
  return apiFetchAuthenticated(`/api/comments/${id}`, {
    method: "DELETE",
  });
}

export function banUser(id) {
  return apiFetchAuthenticated(`/api/users/banUser/${id}`, { 
    method: "PATCH" 
  });
}

export function unbanUser(id) {
  return apiFetchAuthenticated(`/api/users/unbanUser/${id}`, { 
    method: "PATCH" 
  });
}

export function requestContributor() {
  return apiFetchAuthenticated(`/api/users/requestContributor`, { 
    method: "PATCH" 
  });
}

export function getBannedUsers() {
  return apiFetchAuthenticated(`/api/users/getBannedUsers`, { 
    method: "GET" 
  });
}

export function getUnbannedUsers() {
  return apiFetchAuthenticated(`/api/users/getUnbannedUsers`, { 
    method: "GET" 
  });
}

export function getRequestedContributor() {
  return apiFetchAuthenticated(`/api/users/getRequestedContributor`, { 
    method: "GET" 
  });
}

export function promoteToContributor(id) {
  return apiFetchAuthenticated(`/api/users/promoteToContributor/${id}`, { 
    method: "PATCH" 
  });
}

export function denyContributor(id) {
  return apiFetchAuthenticated(`/api/users/denyContributor/${id}`, {
    method: "PATCH"
  });
}

export function getDeniedContributor() {
  return apiFetchAuthenticated(`/api/users/getDeniedContributor`, {
    method: "GET"
  })
}

export function promoteToAdmin(id) {
  return apiFetchAuthenticated(`/api/users/promoteToAdmin/${id}`, {
    method: "PATCH"
  });
}

export function demoteToUser(id) {
  return apiFetchAuthenticated(`/api/users/demoteToUser/${id}`, {
    method: "PATCH"
  });
}
