import { Route, Routes } from 'react-router-dom'
import { Navbar } from './components/NavBar/Navbar'
import { useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import Home from './views/Home/Home'
import Contact from './views/Contact/Contact'
import Karts from './views/Karts/Karts'
import Prices from './views/Prices/Prices'
import Formulario from './views/Forms/Formulario'
import Rack from './views/Rack/Rack'
import Reports from './views/Reports/Reports'
import './App.css'

function App() {

  return (
    <div className="App">
      <Navbar />
      <Routes>
        <Route path="/" element={<Home />} /> 
        <Route path="/contact" element={<Contact />} />
        <Route path="/karts" element={<Karts />} /> 
        <Route path="/prices" element={<Prices />} />
        <Route path="/formulario" element={<Formulario />} />
        <Route path="/rack" element={<Rack />} />
        <Route path="/reports" element={<Reports />} />
      </Routes>
      </div>
  )
}

export default App