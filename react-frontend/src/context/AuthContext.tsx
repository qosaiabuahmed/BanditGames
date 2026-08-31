import React, { createContext, useContext, useEffect, useState, type ReactNode } from 'react';
import { keycloak, initKeycloak } from '../infrastructure/keycloak/keycloakConfig';
import { UserService } from '../services/userService';

interface AuthContextType {
  isAuthenticated: boolean;
  isInitialized: boolean;
  login: () => void;
  logout: () => void;
  register: () => void;
  getToken: () => string | undefined;
  getUsername: () => string | undefined;
  getEmail: () => string | undefined;
  getUserId: () => string | undefined;
  hasRole: (role: string) => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isInitialized, setIsInitialized] = useState(false);

  useEffect(() => {
    initKeycloak(() => {
      const authenticated = !!keycloak.token;
      setIsAuthenticated(authenticated);
      setIsInitialized(true);

      // Debug: Log user roles
      if (authenticated && keycloak.tokenParsed) {
        console.log('👤 User authenticated:', {
          username: keycloak.tokenParsed.preferred_username,
          email: keycloak.tokenParsed.email,
          roles: keycloak.tokenParsed.realm_access?.roles || [],
          hasAdminRole: keycloak.hasRealmRole('ADMIN')
        });
      }

      // Mark user as online when authenticated
      if (authenticated) {
        UserService.markUserOnline().catch((err) => {
          console.error('Failed to mark user as online:', err);
        });
      }
    });

    // Set up token refresh
    const interval = setInterval(() => {
      keycloak.updateToken(70).catch(() => {
        console.error('Failed to refresh token');
      });
    }, 60000); // Refresh every minute

    return () => {
      clearInterval(interval);
    };
  }, []);

  const login = () => {
    keycloak.login();
  };

  const logout = async () => {
    // Mark user as offline before logging out
    try {
      await UserService.markUserOffline();
    } catch (err) {
      console.error('Failed to mark user as offline:', err);
    }
    keycloak.logout();
  };

  const register = () => {
    keycloak.register();
  };

  const getToken = () => {
    return keycloak.token;
  };

  const getUsername = () => {
    return keycloak.tokenParsed?.preferred_username;
  };

  const getEmail = () => {
    return keycloak.tokenParsed?.email;
  };

  const getUserId = () => {
    return keycloak.tokenParsed?.sub;
  };

  const hasRole = (role: string) => {
    return keycloak.hasRealmRole(role);
  };

  const value = {
    isAuthenticated,
    isInitialized,
    login,
    logout,
    register,
    getToken,
    getUsername,
    getEmail,
    getUserId,
    hasRole,
  };

  // Don't render children until Keycloak is initialized
  if (!isInitialized) {
    return (
      <div style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh',
        flexDirection: 'column',
        gap: '1rem'
      }}>
        <div>Initializing authentication...</div>
        <div style={{
          width: '50px',
          height: '50px',
          border: '5px solid #f3f3f3',
          borderTop: '5px solid #3498db',
          borderRadius: '50%',
          animation: 'spin 1s linear infinite'
        }}></div>
        <style>{`
          @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
          }
        `}</style>
      </div>
    );
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};