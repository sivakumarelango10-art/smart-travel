import React, { createContext, useContext, useState, useEffect } from 'react';
import {
  User,
  LoginRequest,
  RegisterRequest,
  UpdateProfileRequest,
  ChangePasswordRequest,
  DeleteAccountRequest
} from '../types/api';
import { authService } from '../services/authService';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  loading: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  loginWithGoogle: (credential: string, rememberMe?: boolean) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => void;
  refreshProfile: () => Promise<void>;
  updateProfile: (data: UpdateProfileRequest) => Promise<User>;
  changePassword: (data: ChangePasswordRequest) => Promise<void>;
  deleteAccount: (data?: DeleteAccountRequest) => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(() => authService.getStoredUser());
  const [loading, setLoading] = useState<boolean>(true);

  const refreshProfile = async () => {
    if (authService.isAuthenticated()) {
      try {
        const res = await authService.getProfile();
        if (res.success && res.data) {
          setUser(res.data);
        }
      } catch {
        // Token invalid/expired, clear state
        authService.logout();
        setUser(null);
      }
    } else {
      setUser(null);
    }
  };

  useEffect(() => {
    const initAuth = async () => {
      try {
        await refreshProfile();
      } finally {
        setLoading(false);
      }
    };
    initAuth();
  }, []);

  const login = async (credentials: LoginRequest) => {
    const res = await authService.login(credentials);
    if (res.success && res.data?.user) {
      setUser(res.data.user);
      // Asynchronously refresh full profile details in background without delaying navigation
      authService.getProfile().then((profRes) => {
        if (profRes.success && profRes.data) {
          setUser(profRes.data);
        }
      }).catch(() => {
        // Fallback remains user summary from login
      });
    }
  };

  const loginWithGoogle = async (credential: string, rememberMe: boolean = true) => {
    const res = await authService.loginWithGoogle(credential, rememberMe);
    if (res.success && res.data?.user) {
      setUser(res.data.user);
      // Asynchronously refresh full profile details in background without delaying navigation
      authService.getProfile().then((profRes) => {
        if (profRes.success && profRes.data) {
          setUser(profRes.data);
        }
      }).catch(() => {
        // Fallback remains user summary from Google login
      });
    }
  };

  const register = async (data: RegisterRequest) => {
    const res = await authService.register(data);
    if (res.success) {
      // Auto-login after registration
      await login({ email: data.email, password: data.password });
    }
  };

  const updateProfile = async (data: UpdateProfileRequest): Promise<User> => {
    const res = await authService.updateProfile(data);
    if (res.success && res.data) {
      setUser(res.data);
      return res.data;
    }
    throw new Error(res.message || 'Failed to update profile');
  };

  const changePassword = async (data: ChangePasswordRequest): Promise<void> => {
    const res = await authService.changePassword(data);
    if (!res.success) {
      throw new Error(res.message || 'Failed to change password');
    }
  };

  const deleteAccount = async (data?: DeleteAccountRequest): Promise<void> => {
    await authService.deleteAccount(data);
    setUser(null);
  };

  const logout = () => {
    authService.logout();
    setUser(null);
  };

  const isAdmin = user?.roles?.includes('ROLE_ADMIN') || user?.roles?.includes('ADMIN') || false;

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        isAdmin,
        loading,
        login,
        loginWithGoogle,
        register,
        logout,
        refreshProfile,
        updateProfile,
        changePassword,
        deleteAccount,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

