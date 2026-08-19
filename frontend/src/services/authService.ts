import { apiClient, TOKEN_KEY, REFRESH_TOKEN_KEY, USER_KEY } from './api';
import { ApiResponse, AuthResponse, LoginRequest, RegisterRequest, User } from '../types/api';

export const authService = {
  async register(data: RegisterRequest): Promise<ApiResponse<User>> {
    const res = await apiClient.post<ApiResponse<User>>('/v1/auth/register', data);
    return res.data;
  },

  async login(credentials: LoginRequest): Promise<ApiResponse<AuthResponse>> {
    const res = await apiClient.post<ApiResponse<AuthResponse>>('/v1/auth/login', credentials);
    if (res.data.success && res.data.data.accessToken) {
      localStorage.setItem(TOKEN_KEY, res.data.data.accessToken);
      if (res.data.data.refreshToken) {
        localStorage.setItem(REFRESH_TOKEN_KEY, res.data.data.refreshToken);
      }
      if (res.data.data.user) {
        localStorage.setItem(USER_KEY, JSON.stringify(res.data.data.user));
      }
    }
    return res.data;
  },

  async getProfile(): Promise<ApiResponse<User>> {
    const res = await apiClient.get<ApiResponse<User>>('/v1/auth/me');
    if (res.data.success && res.data.data) {
      localStorage.setItem(USER_KEY, JSON.stringify(res.data.data));
    }
    return res.data;
  },

  async refreshToken(): Promise<ApiResponse<AuthResponse>> {
    const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);
    const res = await apiClient.post<ApiResponse<AuthResponse>>('/v1/auth/refresh-token', { refreshToken });
    if (res.data.success && res.data.data.accessToken) {
      localStorage.setItem(TOKEN_KEY, res.data.data.accessToken);
    }
    return res.data;
  },

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  },

  getStoredUser(): User | null {
    const userStr = localStorage.getItem(USER_KEY);
    if (!userStr) return null;
    try {
      return JSON.parse(userStr);
    } catch {
      return null;
    }
  },

  isAuthenticated(): boolean {
    return !!localStorage.getItem(TOKEN_KEY);
  },
};
