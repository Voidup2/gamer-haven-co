import { api, setAccessToken } from "./client";

export type LoginRequest = {
  username: string;
  password: string;
};

export type LoginResponse = {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  refreshExpiresIn: number;
  userId: number;
  username: string;
};

export type RegisterRequest = {
  username: string;
  email: string;
  password: string;
};

export type RegisterResponse = Record<string, unknown>;

export async function login(request: LoginRequest) {
  const response = await api.post<LoginResponse>("/auth/login", request);
  setAccessToken(response.accessToken);
  return response;
}

export async function register(request: RegisterRequest) {
  return api.post<RegisterResponse>("/auth/register", request);
}

export async function refresh(refreshToken: string) {
  const response = await api.post<LoginResponse>("/auth/refresh", { refreshToken });
  setAccessToken(response.accessToken);
  return response;
}

export async function logout(refreshToken: string) {
  try {
    await api.post<void>("/auth/logout", { refreshToken });
  } finally {
    setAccessToken(null);
  }
}

export async function logoutAll() {
  try {
    await api.post<void>("/auth/logout-all");
  } finally {
    setAccessToken(null);
  }
}

export async function verifyEmail(token: string) {
  return api.post<void>("/auth/verify-email", { token });
}
