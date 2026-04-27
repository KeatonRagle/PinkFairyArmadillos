// Base URL for API requests
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

// Fetch with authentication (token) for protected endpoints
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

// Fetch without authentication for public endpoints
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

// Register a new user
export function registerUser(user) {
  return apiFetchUnauthenicated("/api/users/register", {
    method: "POST",
    body: JSON.stringify(user),
  });
}

// Login a user
export function login(user) {
  return apiFetchUnauthenicated("/api/users/login", {
    method: "POST",
    body: JSON.stringify(user),
  });
}

// Get the current authenticated user
export function getCurrentUser() {
  return apiFetchAuthenticated("/api/users/findMe", {
    method: "GET",
  });
}

// Get current user's preferences
export function getCurrentUserPrefs() {
  return apiFetchAuthenticated("/api/users/me/prefs", {
    method: "GET",
  });
}

// Add a user preference
export function addUserPref(userPref) {
  return apiFetchAuthenticated("/api/users/me/addPref", {
    method: "POST",
    body: JSON.stringify(userPref)
  });
}

// Delete a user preference
export function deleteUserPref(prefId) {
  return apiFetchAuthenticated(`/api/users/me/deletePref?prefId=${prefId}`, {
    method: "DELETE",
  });
}

// Update the current user's username
export function updateCurrentUsername(name) {
  return apiFetchAuthenticated("/api/users/me/name", {
    method: "PATCH",
    body: JSON.stringify({ name }),
  });
}

// Update the current user's email
export function updateCurrentEmail(email) {
  return apiFetchAuthenticated(`/api/users/me/updateEmail`, {
    method: "PATCH",
    body: JSON.stringify({ email }),
  });
}

// Update the current user's password
export function updateCurrentPassword(password) {
  return apiFetchAuthenticated(`/api/users/me/updatePassword`, {
    method: "PATCH",
    body: JSON.stringify({ password }),
  });
}

// Get all pets
export function getAllPets() {
  return apiFetchAuthenticated("/api/pets/getAll", {
    method: "GET"
  });
}

// Get pets filtered by criteria
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

// Get featured pets for homepage
export function getFeaturedPets() {
  return apiFetchUnauthenicated("/api/featuredPets/getAll", {
    method: "GET",
  });
}

// Get all reviews, optionally filtered by minRating
export function getAllReviews(minRating) {
  const query = typeof minRating === 'number'
    ? `?minRating=${encodeURIComponent(minRating)}`
    : '';

  return apiFetchAuthenticated(`/api/reviews/getAll${query}`, {
    method: "GET",
  });
}

// Submit a new review
export function submitReview(review) {
  return apiFetchAuthenticated("/api/reviews/submitReview", {
    method: "POST",
    body: JSON.stringify(review),
  });
}

// Delete a review by ID
export function deleteReview(id) {
  return apiFetchAuthenticated(`/api/reviews/${id}`, {
    method: "DELETE",
  });
}

// Submit a new adoption site contribution
export function submitSite(contribution) {
  return apiFetchAuthenticated("/api/adoptionSite/submitSite", {
    method: "POST",
    body: JSON.stringify(contribution),
  });
}

// Get all approved adoption sites
export function getApprovedSites() {
  return apiFetchAuthenticated("/api/adoptionSite/getApprovedSites", { 
    method: "GET" 
  });
}

// Get all denied adoption sites
export function getDeniedSites() {
  return apiFetchAuthenticated("/api/adoptionSite/getDeniedSites", { 
    method: "GET" 
  });
}

// Get all pending adoption sites
export function getPendingSites() {
  return apiFetchAuthenticated("/api/adoptionSite/getPendingSites", { 
        method: "GET" 
  });
}

// Approve an adoption site by ID
export function approveSite(id) {
  return apiFetchAuthenticated(`/api/adoptionSite/approveSite/${id}`, { 
    method: "PATCH"
  });
}

// Deny an adoption site by ID
export function denySite(id) {
  return apiFetchAuthenticated(`/api/adoptionSite/denySite/${id}`, { 
    method: "PATCH" 
  });
}

// Submit a new post
export function submitPost(post) {
  return apiFetchAuthenticated("/api/posts/submitPost", {
    method: "POST",
    body: JSON.stringify(post),
  });
}

// Delete a post by ID
export function deletePost(id) {
  return apiFetchAuthenticated(`/api/posts/${id}`, {
    method: "DELETE",
  });
}

// Get all posts
export function getAllPosts() {
  return apiFetchAuthenticated("/api/posts/getAll", {
    method: "GET"
  });
}

// Submit a new comment
export function submitComment(comment) {
  return apiFetchAuthenticated("/api/comments/submitComment", {
    method: "POST",
    body: JSON.stringify(comment),
  });
}

// Get comments for a specific post
export function getCommentsByPost(postId) {
  return apiFetchAuthenticated(`/api/comments/getByPost/${postId}`, {
    method: "GET"
  });
}

// Delete a comment by ID
export function deleteComment(id) {
  return apiFetchAuthenticated(`/api/comments/${id}`, {
    method: "DELETE",
  });
}

// Ban a user by ID
export function banUser(id) {
  return apiFetchAuthenticated(`/api/users/banUser/${id}`, { 
    method: "PATCH" 
  });
}

// Unban a user by ID
export function unbanUser(id) {
  return apiFetchAuthenticated(`/api/users/unbanUser/${id}`, { 
    method: "PATCH" 
  });
}

// Request contributor status for current user
export function requestContributor() {
  return apiFetchAuthenticated(`/api/users/requestContributor`, { 
    method: "PATCH" 
  });
}

// Get all banned users
export function getBannedUsers() {
  return apiFetchAuthenticated(`/api/users/getBannedUsers`, { 
    method: "GET" 
  });
}

// Get all unbanned users
export function getUnbannedUsers() {
  return apiFetchAuthenticated(`/api/users/getUnbannedUsers`, { 
    method: "GET" 
  });
}

// Get all users who requested contributor status
export function getRequestedContributor() {
  return apiFetchAuthenticated(`/api/users/getRequestedContributor`, { 
    method: "GET" 
  });
}

// Promote a user to contributor by ID
export function promoteToContributor(id) {
  return apiFetchAuthenticated(`/api/users/promoteToContributor/${id}`, { 
    method: "PATCH" 
  });
}

// Deny contributor request by ID
export function denyContributor(id) {
  return apiFetchAuthenticated(`/api/users/denyContributor/${id}`, {
    method: "PATCH"
  });
}

// Get all denied contributor requests
export function getDeniedContributor() {
  return apiFetchAuthenticated(`/api/users/getDeniedContributor`, {
    method: "GET"
  })
}

// Promote a user to admin by ID
export function promoteToAdmin(id) {
  return apiFetchAuthenticated(`/api/users/promoteToAdmin/${id}`, {
    method: "PATCH"
  });
}

// Demote a user to regular user by ID
export function demoteToUser(id) {
  return apiFetchAuthenticated(`/api/users/demoteToUser/${id}`, {
    method: "PATCH"
  });
}

