import { createContext, useContext, useEffect, useMemo, useState } from 'react'
import { keycloak, authConfig } from './keycloak'

const AuthContext = createContext({
  enabled: false,
  initialized: false,
  authenticated: false,
  tokenParsed: null,
  username: '',
  roles: [],
  adminRole: authConfig.adminRole,
  login: () => Promise.resolve(),
  logout: () => Promise.resolve(),
  hasRole: () => false,
})

const extractRoles = () => {
  const realmRoles = keycloak?.realmAccess?.roles || []
  const clientRoles = keycloak?.resourceAccess?.[authConfig.clientId]?.roles || []
  return [...new Set([...realmRoles, ...clientRoles])]
}

export const AuthProvider = ({ children }) => {
  const [state, setState] = useState({
    enabled: authConfig.enabled,
    initialized: !authConfig.enabled,
    authenticated: false,
    tokenParsed: null,
    username: '',
    roles: [],
  })

  useEffect(() => {
    if (!authConfig.enabled || !keycloak) {
      return undefined
    }

    let active = true

    const syncState = (authenticated) => {
      if (!active) {
        return
      }

      setState({
        enabled: true,
        initialized: true,
        authenticated,
        tokenParsed: keycloak.tokenParsed || null,
        username: keycloak.tokenParsed?.preferred_username || '',
        roles: extractRoles(),
      })
    }

    keycloak.onAuthSuccess = () => syncState(true)
    keycloak.onAuthRefreshSuccess = () => syncState(true)
    keycloak.onAuthLogout = () => syncState(false)
    keycloak.onTokenExpired = () => {
      keycloak
        .updateToken(30)
        .then(() => syncState(Boolean(keycloak.authenticated)))
        .catch(() => syncState(false))
    }

    keycloak
      .init({
        onLoad: 'check-sso',
        pkceMethod: 'S256',
        checkLoginIframe: false,
        silentCheckSsoRedirectUri: `${window.location.origin}/silent-check-sso.html`,
      })
      .then((authenticated) => syncState(authenticated))
      .catch(() =>
        setState({
          enabled: true,
          initialized: true,
          authenticated: false,
          tokenParsed: null,
          username: '',
          roles: [],
        }),
      )

    return () => {
      active = false
    }
  }, [])

  const value = useMemo(
    () => ({
      ...state,
      adminRole: authConfig.adminRole,
      login: () => (keycloak ? keycloak.login() : Promise.resolve()),
      logout: () =>
        keycloak
          ? keycloak.logout({ redirectUri: window.location.origin })
          : Promise.resolve(),
      hasRole: (role) => state.roles.includes(role),
    }),
    [state],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export const useAuth = () => useContext(AuthContext)
