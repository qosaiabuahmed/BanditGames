import { createContext, useContext, useState, useCallback, ReactNode } from 'react';

interface User {
  userId: string;
  username: string;
  email: string;
  playerTag: string;
}

interface AuthContextType {
  token: string | null;
  user: User | null;
  matchId: string | null;
  setAuth: (token: string, matchId: string) => void;
  setUser: (user: User) => void;
  clearAuth: () => void;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() =>
    sessionStorage.getItem('auth_token')
  );
  const [matchId, setMatchId] = useState<string | null>(() =>
    sessionStorage.getItem('match_id')
  );
  const [user, setUserState] = useState<User | null>(() => {
    const stored = sessionStorage.getItem('user');
    return stored ? JSON.parse(stored) : null;
  });

  const setAuth = useCallback((newToken: string, newMatchId: string) => {
    setToken(newToken);
    setMatchId(newMatchId);
    sessionStorage.setItem('auth_token', newToken);
    sessionStorage.setItem('match_id', newMatchId);
  }, []);

  const setUser = useCallback((newUser: User) => {
    setUserState(newUser);
    sessionStorage.setItem('user', JSON.stringify(newUser));
  }, []);

  const clearAuth = useCallback(() => {
    setToken(null);
    setMatchId(null);
    setUserState(null);
    sessionStorage.removeItem('auth_token');
    sessionStorage.removeItem('match_id');
    sessionStorage.removeItem('user');
  }, []);

  const isAuthenticated = !!token && !!user;

  return (
    <AuthContext.Provider value={{
      token,
      user,
      matchId,
      setAuth,
      setUser,
      clearAuth,
      isAuthenticated
    }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}