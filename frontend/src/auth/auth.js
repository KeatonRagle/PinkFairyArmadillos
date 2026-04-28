// Retrieve the authentication token from localStorage
export function getToken() {
  return localStorage.getItem("token")
}

// Retrieve the username from localStorage
export function getUsername() {
  return localStorage.getItem("username")
}

// changes
// Retrieve the user role from localStorage
export function getRole() {
  return localStorage.getItem("role")
}

// Remove all authentication data from localStorage (logout)
export function logout() {
  localStorage.removeItem("token")
  localStorage.removeItem("username")
  // changes
  localStorage.removeItem("role")
}
