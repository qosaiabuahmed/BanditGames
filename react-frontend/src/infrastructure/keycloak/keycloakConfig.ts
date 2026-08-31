import Keycloak from 'keycloak-js';

// Keycloak configuration
export const keycloakConfig = {
  url: import.meta.env.VITE_KEYCLOAK_URL,
  realm: import.meta.env.VITE_KEYCLOAK_REALM,
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID,
};

// Create Keycloak instance
export const keycloak = new Keycloak(keycloakConfig);

// Initialize Keycloak
export const initKeycloak = (onAuthenticatedCallback: () => void) => {
  keycloak.init({
    onLoad: 'check-sso', // Check if user is already logged in
    pkceMethod: 'S256', // Use PKCE for security
    checkLoginIframe: false, // Disable iframe check
  })
    .then((authenticated) => {
      if (authenticated) {
        console.log('User is authenticated');
      } else {
        console.log('User is not authenticated');
      }
      onAuthenticatedCallback();
    })
    .catch((error) => {
      console.error('Failed to initialize Keycloak:', error);
      onAuthenticatedCallback();
    });
};

// Helper functions
export const login = () => keycloak.login();
export const logout = () => keycloak.logout();
export const register = () => keycloak.register();
export const getToken = () => keycloak.token;
export const isLoggedIn = () => !!keycloak.token;
export const updateToken = (successCallback: () => void) =>
  keycloak.updateToken(70).then(successCallback).catch(login);
export const getUsername = () => keycloak.tokenParsed?.preferred_username;
export const getEmail = () => keycloak.tokenParsed?.email;
export const getUserId = () => keycloak.tokenParsed?.sub;
export const hasRole = (role: string) => keycloak.hasRealmRole(role);