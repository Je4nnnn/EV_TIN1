import { Box, Container } from '@mui/material'
import { Route, Routes } from 'react-router-dom'
import { Navbar } from './components/NavBar/Navbar'
import Home from './views/Home/Home'
import Contact from './views/Contact/Contact'
import Rooms from './views/Rooms/Rooms'
import Prices from './views/Prices/Prices'
import Formulario from './views/Forms/Formulario'
import Rack from './views/Rack/Rack'
import Reports from './views/Reports/Reports'
import TouristPackages from './views/TouristPackages/TouristPackages'
import { RequireRole } from './components/auth/RequireRole'
import { useAuth } from './auth/AuthContext'
import './App.css'

function App() {
  const auth = useAuth()

  return (
    <Box className="App">
      <Navbar />
      <Container maxWidth="xl" sx={{ py: 4 }}>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/contact" element={<Contact />} />
          <Route path="/rooms" element={<Rooms />} />
          <Route path="/prices" element={<Prices />} />
          <Route path="/formulario" element={<Formulario />} />
          <Route
            path="/rack"
            element={
              <RequireRole role={auth.adminRole}>
                <Rack />
              </RequireRole>
            }
          />
          <Route
            path="/reports"
            element={
              <RequireRole role={auth.adminRole}>
                <Reports />
              </RequireRole>
            }
          />
          <Route
            path="/tourist-packages"
            element={
              <RequireRole role={auth.adminRole}>
                <TouristPackages />
              </RequireRole>
            }
          />
        </Routes>
      </Container>
    </Box>
  )
}

export default App
