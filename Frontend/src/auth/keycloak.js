import Keycloak from 'keycloak-js'

const enabled = import.meta.env.VITE_KEYCLOAK_ENABLED !== 'false'
const url = import.meta.env.VITE_KEYCLOAK_URL || 'http://localhost:8080'
const realm = import.meta.env.VITE_KEYCLOAK_REALM || 'hotelrm'
const clientId = import.meta.env.VITE_KEYCLOAK_CLIENT_ID || 'hotelrm-frontend'
const adminRole = import.meta.env.VITE_KEYCLOAK_ADMIN_ROLE || 'hotelrm_admin'

export const authConfig = {
  enabled,
  url,
  realm,
  clientId,
  adminRole,
}

export const keycloak =
  enabled && url && realm && clientId
    ? new Keycloak({
        url,
        realm,
        clientId,
      })
    : null

export const getValidAccessToken = async () => {
  if (!keycloak || !keycloak.authenticated) {
    return null
  }

  try {
    await keycloak.updateToken(30)
  } catch (error) {
    return keycloak.token || null
  }

  return keycloak.token || null
}
