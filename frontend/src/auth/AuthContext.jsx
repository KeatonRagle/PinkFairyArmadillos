import { createContext, useContext, useEffect, useState } from "react";

// Create the authentication context
const AuthContext = createContext(null);

// Provider component to supply authentication state and actions
export function AuthProvider({ children }) {
  // changes
  const [token, setToken] = useState(null);
  const [username, setUsername] = useState(null);
  const [role, setRole] = useState(null);
  const [id, setId] = useState(null);

    // Initialize authentication state from localStorage on mount
  useEffect(() => {
    setToken(localStorage.getItem("token"));
    setUsername(localStorage.getItem("username"));
    // changes
    setRole(localStorage.getItem("role"));
    setId(localStorage.getItem("id"));
  }, []);

  // changes
    // Set authentication state and persist to localStorage
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

    // Clear authentication state and remove from localStorage
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
    // Get the current user role
  const getRole = () => role;

    // Provide authentication context to children
  return (
    <AuthContext.Provider value={{ token, username, role, id, getRole, setAuth, clearAuth }}>
      {children}
    </AuthContext.Provider>
  );
}

// Hook to access authentication context
export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within an AuthProvider");
  return ctx;
}
