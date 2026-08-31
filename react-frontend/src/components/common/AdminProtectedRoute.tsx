import type { ReactNode } from 'react';
import { useAuth } from '../../context/AuthContext';
import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

interface AdminProtectedRouteProps {
  children: ReactNode;
}

export default function AdminProtectedRoute({ children }: AdminProtectedRouteProps) {
  const { isAuthenticated, hasRole, login } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isAuthenticated) {
      login();
      return;
    }

    if (!hasRole('ADMIN')) {
      // Not an admin - redirect to regular dashboard
      navigate('/dashboard');
    }
  }, [isAuthenticated, hasRole, login, navigate]);

  if (!isAuthenticated) {
    return (
      <div style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh'
      }}>
        Redirecting to login...
      </div>
    );
  }

  if (!hasRole('ADMIN')) {
    return (
      <div style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh'
      }}>
        Access denied. Redirecting...
      </div>
    );
  }

  return <>{children}</>;
}