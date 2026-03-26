import { createContext, useContext, useMemo, useState, type ReactNode } from "react";
import { clearSession, tokenStorage, userStorage } from "../lib/storage";
import type { AuthResponse, UserSummary } from "../types";
import { authService } from "../services/authService";

interface AuthContextType {
  user: UserSummary | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (username: string, email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const applyAuthPayload = (payload: AuthResponse) => {
  tokenStorage.setAccessToken(payload.accessToken);
  tokenStorage.setRefreshToken(payload.refreshToken);
  userStorage.set(payload.userSummary);
};

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserSummary | null>(() => userStorage.get());

  const login = async (email: string, password: string) => {
    const response = await authService.login(email, password);
    const payload = response.data.data;
    applyAuthPayload(payload);
    setUser(payload.userSummary);
  };

  const register = async (username: string, email: string, password: string) => {
    const response = await authService.register(username, email, password);
    const payload = response.data.data;
    applyAuthPayload(payload);
    setUser(payload.userSummary);
  };

  const logout = async () => {
    try {
      await authService.logout();
    } finally {
      clearSession();
      setUser(null);
    }
  };

  const value = useMemo(
    () => ({
      user,
      isAuthenticated: Boolean(user && tokenStorage.getAccessToken()),
      login,
      register,
      logout,
    }),
    [user]
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
