import React, { createContext, useContext, useState, useEffect } from 'react';
import { User, LoginRequest, RegisterRequest } from '../types/api';
import { authService } from '../services/authService';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  loading: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => void;
  refreshProfile: () => Promise<void>;
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
      } catch (err) {
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
    }
  };

  const register = async (data: RegisterRequest) => {
    const res = await authService.register(data);
    if (res.success) {
      // Auto-login after registration
      await login({ email: data.email, password: data.password });
    }
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
        register,
        logout,
        refreshProfile,
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
