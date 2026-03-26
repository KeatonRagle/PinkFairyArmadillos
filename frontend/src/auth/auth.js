export function getToken() {
  return localStorage.getItem("token")
}

export function getUsername() {
  return localStorage.getItem("username")
}

// changes
export function getRole() {
  return localStorage.getItem("role")
}

export function logout() {
  localStorage.removeItem("token")
  localStorage.removeItem("username")
  // changes
  localStorage.removeItem("role")
}
