import { api } from "../lib/api";
import type { ApiResponse, AuthResponse } from "../types";

export const authService = {
  login(email: string, password: string) {
    return api.post<ApiResponse<AuthResponse>>("/auth/login", { email, password });
  },
  register(username: string, email: string, password: string) {
    return api.post<ApiResponse<AuthResponse>>("/auth/register", { username, email, password });
  },
  logout() {
    return api.post("/auth/logout");
  },
};
