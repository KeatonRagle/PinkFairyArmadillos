export function getToken() {
  return localStorage.getItem("token")
}

export function getUsername() {
  return localStorage.getItem("username")
}

export function logout() {
  localStorage.removeItem("token")
  localStorage.removeItem("username")
}
