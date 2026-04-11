import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';
import {
  AUTH_TOKEN_STORAGE_KEY,
  ApiError,
  getCurrentUser,
  loginWithPassword,
  logoutRequest,
  registerWithEmail,
} from '../services/api';
import type { AuthUser } from '../types';

interface AuthContextValue {
  user: AuthUser | null;
  loading: boolean;
  isAuthenticated: boolean;
  isBanned: boolean;
  login: (username: string, password: string) => Promise<void>;
  register: (payload: { email: string; code: string; username: string; password: string }) => Promise<void>;
  logout: () => Promise<void>;
  hasPermission: (permissionCode: string) => boolean;
  hasRole: (roleCode: string) => boolean;
}

const AuthContext = createContext<AuthContextValue | null>(null);

function persistToken(token: string) {
  window.localStorage.setItem(AUTH_TOKEN_STORAGE_KEY, token);
}

function clearToken() {
  window.localStorage.removeItem(AUTH_TOKEN_STORAGE_KEY);
}

function getToken() {
  return window.localStorage.getItem(AUTH_TOKEN_STORAGE_KEY) ?? '';
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;

    async function bootstrap() {
      const token = getToken();
      if (!token) {
        setLoading(false);
        return;
      }

      try {
        const currentUser = await getCurrentUser();
        if (!cancelled) {
          setUser(currentUser);
        }
      } catch (error) {
        clearToken();
        if (!cancelled) {
          setUser(null);
        }
        if (!(error instanceof ApiError) || error.status !== 401) {
          console.error('恢复登录态失败。', error);
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    void bootstrap();

    return () => {
      cancelled = true;
    };
  }, []);

  async function login(username: string, password: string) {
    const payload = await loginWithPassword({ username, password });
    persistToken(payload.token);
    setUser(payload.user);
  }

  async function register(payload: { email: string; code: string; username: string; password: string }) {
    const result = await registerWithEmail(payload);
    persistToken(result.token);
    setUser(result.user);
  }

  async function logout() {
    try {
      if (getToken()) {
        await logoutRequest();
      }
    } catch (error) {
      console.error('退出登录失败。', error);
    } finally {
      clearToken();
      setUser(null);
    }
  }

  function hasPermission(permissionCode: string) {
    return user?.permissions?.some((permission) => permission.code === permissionCode) ?? false;
  }

  function hasRole(roleCode: string) {
    return user?.roles?.some((role) => role.code === roleCode) ?? false;
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        isAuthenticated: Boolean(user),
        isBanned: user?.accountStatus === 'BANNED',
        login,
        register,
        logout,
        hasPermission,
        hasRole,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuthContext() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuthContext 必须在 AuthProvider 内使用');
  }

  return context;
}
