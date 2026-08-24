import { apiClient, TOKEN_KEY, REFRESH_TOKEN_KEY, USER_KEY } from './api';
import {
  ApiResponse,
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  UpdateProfileRequest,
  ChangePasswordRequest,
  DeleteAccountRequest,
  User
} from '../types/api';

export const authService = {
  async register(data: RegisterRequest): Promise<ApiResponse<User>> {
    const res = await apiClient.post<ApiResponse<User>>('/v1/auth/register', data);
    return res.data;
  },

  async login(credentials: LoginRequest): Promise<ApiResponse<AuthResponse>> {
    const res = await apiClient.post<ApiResponse<AuthResponse>>('/v1/auth/login', credentials);
    if (res.data.success && res.data.data.accessToken) {
      const storage = credentials.rememberMe ? localStorage : sessionStorage;
      // Clear alternative storage to prevent state split
      const altStorage = credentials.rememberMe ? sessionStorage : localStorage;
      altStorage.removeItem(TOKEN_KEY);
      altStorage.removeItem(REFRESH_TOKEN_KEY);
      altStorage.removeItem(USER_KEY);

      storage.setItem(TOKEN_KEY, res.data.data.accessToken);
      if (res.data.data.refreshToken) {
        storage.setItem(REFRESH_TOKEN_KEY, res.data.data.refreshToken);
      }
      if (res.data.data.user) {
        storage.setItem(USER_KEY, JSON.stringify(res.data.data.user));
      }
    }
    return res.data;
  },

  async getProfile(): Promise<ApiResponse<User>> {
    const res = await apiClient.get<ApiResponse<User>>('/v1/auth/me');
    if (res.data.success && res.data.data) {
      const storage = localStorage.getItem(TOKEN_KEY) ? localStorage : sessionStorage;
      storage.setItem(USER_KEY, JSON.stringify(res.data.data));
    }
    return res.data;
  },

  async updateProfile(data: UpdateProfileRequest): Promise<ApiResponse<User>> {
    const res = await apiClient.put<ApiResponse<User>>('/v1/auth/me', data);
    if (res.data.success && res.data.data) {
      const storage = localStorage.getItem(TOKEN_KEY) ? localStorage : sessionStorage;
      storage.setItem(USER_KEY, JSON.stringify(res.data.data));
    }
    return res.data;
  },

  async changePassword(data: ChangePasswordRequest): Promise<ApiResponse<void>> {
    const res = await apiClient.put<ApiResponse<void>>('/v1/auth/password', data);
    return res.data;
  },

  async deleteAccount(data?: DeleteAccountRequest): Promise<ApiResponse<void>> {
    const res = await apiClient.delete<ApiResponse<void>>('/v1/auth/me', { data });
    this.logout();
    return res.data;
  },

  async refreshToken(): Promise<ApiResponse<AuthResponse>> {
    const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY) || sessionStorage.getItem(REFRESH_TOKEN_KEY);
    const res = await apiClient.post<ApiResponse<AuthResponse>>('/v1/auth/refresh-token', { refreshToken });
    if (res.data.success && res.data.data.accessToken) {
      const storage = localStorage.getItem(TOKEN_KEY) ? localStorage : sessionStorage;
      storage.setItem(TOKEN_KEY, res.data.data.accessToken);
    }
    return res.data;
  },

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    sessionStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(REFRESH_TOKEN_KEY);
    sessionStorage.removeItem(USER_KEY);
  },

  getStoredUser(): User | null {
    const userStr = localStorage.getItem(USER_KEY) || sessionStorage.getItem(USER_KEY);
    if (!userStr) return null;
    try {
      return JSON.parse(userStr);
    } catch {
      return null;
    }
  },

  async getPreferences(): Promise<ApiResponse<any>> {
    const res = await apiClient.get<ApiResponse<any>>('/v1/auth/preferences');
    return res.data;
  },

  async updatePreferences(preferences: any): Promise<ApiResponse<any>> {
    const res = await apiClient.put<ApiResponse<any>>('/v1/auth/preferences', preferences);
    return res.data;
  },

  isAuthenticated(): boolean {
    return !!(localStorage.getItem(TOKEN_KEY) || sessionStorage.getItem(TOKEN_KEY));
  },
};

