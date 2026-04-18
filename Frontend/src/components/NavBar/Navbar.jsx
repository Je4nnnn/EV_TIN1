import { useState } from 'react'
import MenuIcon from '@mui/icons-material/Menu'
import {
  AppBar,
  Box,
  Button,
  Drawer,
  IconButton,
  Stack,
  Toolbar,
  Typography,
} from '@mui/material'
import { Link as RouterLink, useLocation } from 'react-router-dom'
import { useAuth } from '../../auth/AuthContext'

const navigationItems = [
  { to: '/rooms', label: 'Habitaciones' },
  { to: '/prices', label: 'Precios' },
  { to: '/rack', label: 'Rack semanal', requiresAdmin: true },
  { to: '/reports', label: 'Reportes', requiresAdmin: true },
  { to: '/tourist-packages', label: 'Paquetes turisticos', requiresAdmin: true },
]

export const Navbar = () => {
  const [menuOpen, setMenuOpen] = useState(false)
  const location = useLocation()
  const auth = useAuth()

  const visibleNavigationItems = navigationItems.filter(
    ({ requiresAdmin }) => !requiresAdmin || !auth.enabled || auth.hasRole(auth.adminRole),
  )

  const renderNavButton = ({ to, label }) => (
    <Button
      key={to}
      component={RouterLink}
      to={to}
      color="inherit"
      variant={location.pathname === to ? 'contained' : 'text'}
      sx={{
        borderRadius: 99,
        px: 2,
        color: location.pathname === to ? 'white' : 'inherit',
      }}
      onClick={() => setMenuOpen(false)}
    >
      {label}
    </Button>
  )

  return (
    <AppBar
      position="sticky"
      elevation={0}
      sx={{
        backdropFilter: 'blur(14px)',
        backgroundColor: 'rgba(11, 31, 36, 0.9)',
        borderBottom: '1px solid rgba(255,255,255,0.08)',
      }}
    >
      <Toolbar sx={{ display: 'flex', justifyContent: 'space-between', gap: 2 }}>
        <Typography
          component={RouterLink}
          to="/"
          variant="h5"
          sx={{ fontWeight: 800, letterSpacing: 0.3 }}
        >
          HotelRM
        </Typography>

        <Stack
          direction="row"
          spacing={1}
          sx={{ display: { xs: 'none', md: 'flex' }, alignItems: 'center' }}
        >
          {visibleNavigationItems.map(renderNavButton)}
          {auth.enabled ? (
            auth.authenticated ? (
              <>
                <Typography variant="body2" sx={{ ml: 1 }}>
                  {auth.username}
                </Typography>
                <Button color="inherit" variant="outlined" onClick={() => auth.logout()}>
                  Cerrar sesion
                </Button>
              </>
            ) : (
              <Button color="inherit" variant="outlined" onClick={() => auth.login()}>
                Iniciar sesion
              </Button>
            )
          ) : null}
        </Stack>

        <IconButton
          color="inherit"
          sx={{ display: { xs: 'inline-flex', md: 'none' } }}
          onClick={() => setMenuOpen(true)}
        >
          <MenuIcon />
        </IconButton>
      </Toolbar>

      <Drawer anchor="right" open={menuOpen} onClose={() => setMenuOpen(false)}>
        <Box sx={{ width: 280, p: 2 }}>
          <Stack spacing={1.5}>
            <Typography variant="h6" sx={{ fontWeight: 700 }}>
              Navegacion
            </Typography>
            {visibleNavigationItems.map(renderNavButton)}
            {auth.enabled ? (
              auth.authenticated ? (
                <Button variant="outlined" onClick={() => auth.logout()}>
                  Cerrar sesion
                </Button>
              ) : (
                <Button variant="contained" onClick={() => auth.login()}>
                  Iniciar sesion
                </Button>
              )
            ) : null}
          </Stack>
        </Box>
      </Drawer>
    </AppBar>
  )
}
