import type { ReactNode } from 'react';
import { useAuth } from '../../context/AuthContext';
import { useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

interface ProtectedRouteProps {
  children: ReactNode;
}

export default function ProtectedRoute({ children }: ProtectedRouteProps) {
  const { isAuthenticated, login, hasRole } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    if (!isAuthenticated) {
      login();
      return;
    }

    // Debug: Check if user has ADMIN role
    const isAdmin = hasRole('ADMIN');
    console.log('🔍 ProtectedRoute Debug:', {
      isAuthenticated,
      isAdmin,
      currentPath: location.pathname,
      willRedirect: isAdmin && location.pathname === '/dashboard'
    });

    // Auto-redirect admins to admin dashboard instead of regular dashboard
    if (isAuthenticated && isAdmin && location.pathname === '/dashboard') {
      console.log('✅ Redirecting admin to /admin dashboard');
      navigate('/admin', { replace: true });
    }
  }, [isAuthenticated, hasRole, login, navigate, location.pathname]);

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

  return <>{children}</>;
}