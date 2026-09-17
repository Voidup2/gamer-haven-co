import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from "react";

import {
  login as loginRequest,
  logout as logoutRequest,
  refresh as refreshRequest,
  register as registerRequest,
  type LoginRequest,
  type LoginResponse,
  type RegisterRequest,
} from "@/api/auth";
import { setAccessToken } from "@/api/client";

const STORAGE_KEY = "gamesphere.auth";

type AuthSession = LoginResponse;

type AuthContextValue = {
  session: AuthSession | null;
  isAuthenticated: boolean;
  isInitializing: boolean;
  login: (request: LoginRequest) => Promise<AuthSession>;
  register: (request: RegisterRequest) => Promise<unknown>;
  logout: () => Promise<void>;
  logoutAll: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function readStoredSession(): AuthSession | null {
  try {
    const stored = sessionStorage.getItem(STORAGE_KEY);
    return stored ? (JSON.parse(stored) as AuthSession) : null;
  } catch {
    sessionStorage.removeItem(STORAGE_KEY);
    return null;
  }
}

function saveSession(session: AuthSession) {
  sessionStorage.setItem(STORAGE_KEY, JSON.stringify(session));
  setAccessToken(session.accessToken);
}

function clearSession() {
  sessionStorage.removeItem(STORAGE_KEY);
  setAccessToken(null);
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<AuthSession | null>(null);
  const [isInitializing, setIsInitializing] = useState(true);

  const applySession = useCallback((nextSession: AuthSession) => {
    saveSession(nextSession);
    setSession(nextSession);
  }, []);

  const clearAuth = useCallback(() => {
    clearSession();
    setSession(null);
  }, []);

  const login = useCallback(async (request: LoginRequest) => {
    const nextSession = await loginRequest(request);
    applySession(nextSession);
    return nextSession;
  }, [applySession]);

  const register = useCallback((request: RegisterRequest) => registerRequest(request), []);

  const logout = useCallback(async () => {
    const refreshToken = session?.refreshToken;
    try {
      if (refreshToken) {
        await logoutRequest(refreshToken);
      }
    } finally {
      clearAuth();
    }
  }, [clearAuth, session?.refreshToken]);

  const logoutAll = useCallback(async () => {
    try {
      const { api } = await import("@/api/client");
      await api.post<void>("/auth/logout-all");
    } finally {
      clearAuth();
    }
  }, [clearAuth]);

  useEffect(() => {
    let cancelled = false;

    async function restoreSession() {
      const stored = readStoredSession();

      if (!stored) {
        setIsInitializing(false);
        return;
      }

      setAccessToken(stored.accessToken);

      try {
        const nextSession = await refreshRequest(stored.refreshToken);
        if (!cancelled) {
          applySession(nextSession);
        }
      } catch {
        if (!cancelled) {
          clearAuth();
        }
      } finally {
        if (!cancelled) {
          setIsInitializing(false);
        }
      }
    }

    void restoreSession();

    return () => {
      cancelled = true;
    };
  }, [applySession, clearAuth]);

  const value = useMemo<AuthContextValue>(() => ({
    session,
    isAuthenticated: session !== null,
    isInitializing,
    login,
    register,
    logout,
    logoutAll,
  }), [isInitializing, login, logout, logoutAll, register, session]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used inside AuthProvider");
  }
  return context;
}
