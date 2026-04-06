import { Route, Routes } from 'react-router-dom'
import { Navbar } from './components/NavBar/Navbar'
import Home from './views/Home/Home'
import Contact from './views/Contact/Contact'
import Rooms from './views/Rooms/Rooms'
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
        <Route path="/rooms" element={<Rooms />} /> 
        <Route path="/prices" element={<Prices />} />
        <Route path="/formulario" element={<Formulario />} />
        <Route path="/rack" element={<Rack />} />
        <Route path="/reports" element={<Reports />} />
      </Routes>
      </div>
  )
}

export default App