import { createContext, useContext, useEffect, useMemo, useState } from "react";
import { api } from "./api";

const TOKEN_KEY = "astro_token";
const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_KEY));
  const [user, setUser] = useState(null);
  const [ready, setReady] = useState(!localStorage.getItem(TOKEN_KEY));

  useEffect(() => {
    if (!token) {
      setUser(null);
      setReady(true);
      return;
    }

    let cancelled = false;
    api("/api/auth/me", { token })
      .then((me) => {
        if (!cancelled) {
          setUser(me);
          setReady(true);
        }
      })
      .catch(() => {
        if (!cancelled) {
          localStorage.removeItem(TOKEN_KEY);
          setToken(null);
          setUser(null);
          setReady(true);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [token]);

  const value = useMemo(
    () => ({
      token,
      user,
      ready,
      async login(email, password) {
        const result = await api("/api/auth/login", {
          method: "POST",
          body: { email, password },
        });
        localStorage.setItem(TOKEN_KEY, result.token);
        setToken(result.token);
        setUser(result.user);
      },
      async signup(email, password) {
        const result = await api("/api/auth/register", {
          method: "POST",
          body: { email, password },
        });
        localStorage.setItem(TOKEN_KEY, result.token);
        setToken(result.token);
        setUser(result.user);
      },
      logout() {
        localStorage.removeItem(TOKEN_KEY);
        setToken(null);
        setUser(null);
      },
    }),
    [token, user, ready],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}
