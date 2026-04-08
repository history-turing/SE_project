import type { ReactElement } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import { AppShell } from './components/AppShell';
import { AppProvider } from './context/AppContext';
import { useAuthContext } from './context/AuthContext';
import { AlumniPage } from './pages/AlumniPage';
import { HomePage } from './pages/HomePage';
import { LoginPage } from './pages/LoginPage';
import { ProfilePage } from './pages/ProfilePage';
import { RegisterPage } from './pages/RegisterPage';
import { TopicsPage } from './pages/TopicsPage';

function AuthLoading() {
  return (
    <div className="auth-shell">
      <div className="auth-card auth-card--compact">
        <p className="eyebrow">武大树洞</p>
        <h1>正在恢复登录状态</h1>
        <p className="auth-copy">请稍候，系统正在确认你的账号会话。</p>
      </div>
    </div>
  );
}

function ProtectedApp() {
  const { loading, isAuthenticated } = useAuthContext();

  if (loading) {
    return <AuthLoading />;
  }

  if (!isAuthenticated) {
    return <Navigate replace to="/login" />;
  }

  return (
    <AppProvider>
      <AppShell />
    </AppProvider>
  );
}

function GuestOnly({ children }: { children: ReactElement }) {
  const { loading, isAuthenticated } = useAuthContext();

  if (loading) {
    return <AuthLoading />;
  }

  if (isAuthenticated) {
    return <Navigate replace to="/" />;
  }

  return children;
}

export default function App() {
  return (
    <Routes>
      <Route
        path="/login"
        element={
          <GuestOnly>
            <LoginPage />
          </GuestOnly>
        }
      />
      <Route
        path="/register"
        element={
          <GuestOnly>
            <RegisterPage />
          </GuestOnly>
        }
      />
      <Route element={<ProtectedApp />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/topics" element={<TopicsPage />} />
        <Route path="/alumni" element={<AlumniPage />} />
        <Route path="/profile" element={<ProfilePage />} />
      </Route>
      <Route path="*" element={<Navigate replace to="/" />} />
    </Routes>
  );
}
