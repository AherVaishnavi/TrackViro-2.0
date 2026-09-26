import { createContext, useContext, useEffect, useState } from "react";
import authApi from "../services/authApi";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // On first load (or a page refresh), a token may already be sitting
  // in sessionStorage from an earlier login. Re-fetch the current user
  // from the backend rather than trusting whatever was cached, so a
  // deactivated account or an expired token is caught immediately.
  useEffect(() => {
    const token = sessionStorage.getItem("trackviro_token");
    if (!token) {
      setLoading(false);
      return;
    }
    authApi
      .me()
      .then((freshUser) => {
        setUser(freshUser);
        sessionStorage.setItem("trackviro_user", JSON.stringify(freshUser));
      })
      .catch(() => {
        // axiosClient's response interceptor already clears storage
        // and redirects on a 401 — nothing extra to do here.
        setUser(null);
      })
      .finally(() => setLoading(false));
  }, []);

  async function login(email, password) {
    const response = await authApi.login(email, password);
    sessionStorage.setItem("trackviro_token", response.token);
    sessionStorage.setItem("trackviro_user", JSON.stringify(response.user));
    setUser(response.user);
    return response.user;
  }

  function logout() {
    // No server-side call: this is a stateless JWT API with no session
    // to invalidate. "Logging out" is simply discarding the token.
    sessionStorage.removeItem("trackviro_token");
    sessionStorage.removeItem("trackviro_user");
    setUser(null);
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
