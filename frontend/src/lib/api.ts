import axios, { AxiosError, type InternalAxiosRequestConfig } from "axios";
import { clearSession, tokenStorage } from "./storage";
import type { ApiResponse, AuthResponse } from "../types";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "/api";

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

let isRefreshing = false;
let pendingRequests: Array<(token: string | null) => void> = [];

const queuePendingRequest = (cb: (token: string | null) => void) => {
  pendingRequests.push(cb);
};

const flushPendingRequests = (token: string | null) => {
  pendingRequests.forEach((cb) => cb(token));
  pendingRequests = [];
};

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const accessToken = tokenStorage.getAccessToken();
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };
    const status = error.response?.status;

    if (status !== 401 || !originalRequest || originalRequest._retry) {
      return Promise.reject(error);
    }

    originalRequest._retry = true;
    const refreshToken = tokenStorage.getRefreshToken();
    if (!refreshToken) {
      clearSession();
      return Promise.reject(error);
    }

    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        queuePendingRequest((newToken) => {
          if (!newToken) {
            reject(error);
            return;
          }
          originalRequest.headers.Authorization = `Bearer ${newToken}`;
          resolve(api(originalRequest));
        });
      });
    }

    isRefreshing = true;
    try {
      const refreshResponse = await axios.post<ApiResponse<AuthResponse>>(
        `${API_BASE_URL}/auth/refresh`,
        { refreshToken }
      );
      const payload = refreshResponse.data.data;
      tokenStorage.setAccessToken(payload.accessToken);
      tokenStorage.setRefreshToken(payload.refreshToken);
      flushPendingRequests(payload.accessToken);

      originalRequest.headers.Authorization = `Bearer ${payload.accessToken}`;
      return api(originalRequest);
    } catch (refreshError) {
      clearSession();
      flushPendingRequests(null);
      return Promise.reject(refreshError);
    } finally {
      isRefreshing = false;
    }
  }
);
