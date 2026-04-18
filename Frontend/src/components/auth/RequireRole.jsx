import { Alert, Box, Button, CircularProgress, Stack, Typography } from '@mui/material'
import { useAuth } from '../../auth/AuthContext'

export const RequireRole = ({ role, children }) => {
  const auth = useAuth()

  if (!auth.enabled) {
    return children
  }

  if (!auth.initialized) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
        <CircularProgress />
      </Box>
    )
  }

  if (!auth.authenticated) {
    return (
      <Stack spacing={2} sx={{ py: 6, maxWidth: 560 }}>
        <Typography variant="h4">Acceso restringido</Typography>
        <Alert severity="info">
          Debes iniciar sesion con Keycloak para acceder a esta vista administrativa.
        </Alert>
        <Box>
          <Button variant="contained" onClick={() => auth.login()}>
            Iniciar sesion
          </Button>
        </Box>
      </Stack>
    )
  }

  if (!auth.hasRole(role)) {
    return (
      <Stack spacing={2} sx={{ py: 6, maxWidth: 640 }}>
        <Typography variant="h4">Permisos insuficientes</Typography>
        <Alert severity="error">
          Tu usuario esta autenticado, pero no tiene el rol requerido: <strong>{role}</strong>.
        </Alert>
      </Stack>
    )
  }

  return children
}
