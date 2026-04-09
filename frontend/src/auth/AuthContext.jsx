import { createContext, useContext, useEffect, useState } from "react";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  // changes
  const [token, setToken] = useState(null);
  const [username, setUsername] = useState(null);
  const [role, setRole] = useState(null);
  const [id, setId] = useState(null);

  useEffect(() => {
    setToken(localStorage.getItem("token"));
    setUsername(localStorage.getItem("username"));
    // changes
    setRole(localStorage.getItem("role"));
    setId(localStorage.getItem("id"));
  }, []);

  // changes
  const setAuth = (newToken, newUsername, newRole, newId) => {
    localStorage.setItem("token", newToken);
    localStorage.setItem("username", newUsername);
    localStorage.setItem("role", newRole);
    localStorage.setItem("id", newId);
    setToken(newToken);
    setUsername(newUsername);
    setRole(newRole);
    setId(newId);
  };

  const clearAuth = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("username");
    // changes
    localStorage.removeItem("role");
    localStorage.removeItem("id");
    setToken(null);
    setUsername(null);
    setRole(null);
    setId(null);
  };

  // changes
  const getRole = () => role;

  return (
    <AuthContext.Provider value={{ token, username, role, id, getRole, setAuth, clearAuth }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within an AuthProvider");
  return ctx;
}
