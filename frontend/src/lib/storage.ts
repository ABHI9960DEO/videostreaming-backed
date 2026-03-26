const ACCESS_TOKEN_KEY = "vs_access_token";
const REFRESH_TOKEN_KEY = "vs_refresh_token";
const USER_KEY = "vs_user";

export const tokenStorage = {
  getAccessToken: () => localStorage.getItem(ACCESS_TOKEN_KEY),
  setAccessToken: (token: string) => localStorage.setItem(ACCESS_TOKEN_KEY, token),
  clearAccessToken: () => localStorage.removeItem(ACCESS_TOKEN_KEY),

  getRefreshToken: () => localStorage.getItem(REFRESH_TOKEN_KEY),
  setRefreshToken: (token: string) => localStorage.setItem(REFRESH_TOKEN_KEY, token),
  clearRefreshToken: () => localStorage.removeItem(REFRESH_TOKEN_KEY),
};

export const userStorage = {
  get: () => {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  },
  set: (user: unknown) => localStorage.setItem(USER_KEY, JSON.stringify(user)),
  clear: () => localStorage.removeItem(USER_KEY),
};

export const clearSession = () => {
  tokenStorage.clearAccessToken();
  tokenStorage.clearRefreshToken();
  userStorage.clear();
};
