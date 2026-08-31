import { keycloak } from '../infrastructure/keycloak/keycloakConfig';

/**
 * Gets the current access token from Keycloak
 * Automatically refreshes the token if needed
 */
export const getAccessToken = async (): Promise<string | null> => {
  try {
    // Try to refresh token if it's about to expire (within 70 seconds)
    await keycloak.updateToken(70);
    return keycloak.token || null;
  } catch (error) {
    console.error('Failed to refresh token', error);
    // If refresh fails, try to re-login
    keycloak.login();
    return null;
  }
};

/**
 * Gets authorization headers with Bearer token
 * @param required - If true, will trigger login if token is not available. If false, returns headers without token.
 */
export const getAuthHeaders = async (required: boolean = true): Promise<HeadersInit> => {
  const token = required ? await getAccessToken() : keycloak.token;
  return {
    'Content-Type': 'application/json',
    ...(token && { 'Authorization': `Bearer ${token}` }),
  };
};
