import React, { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { processParticipants, confirmReserve } from '../../services/ReservationService';
import './Formulario.css';

const STAY_LABELS = {
  Mañana:   'Turno Mañana (08:00 – 18:00)',
  Noche:    'Turno Noche (18:00 – 08:00)',
  Completo: 'Día Completo (24h)',
};

const Formulario = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { dia, diaSalida, tipoDuracion, tipoEstancia } = location.state || {};

  const [cantidadPersonas, setCantidadPersonas] = useState(1);
  const [personas, setPersonas] = useState([
    { nombre: '', rut: '', fechaCumpleanos: '', email: '', telefono: '' },
  ]);
  const [cargando, setCargando] = useState(false);

  const handleCantidadChange = (e) => {
    const cantidad = Math.max(1, Math.min(15, parseInt(e.target.value) || 1));
    setCantidadPersonas(cantidad);

    const nuevasPersonas = Array.from({ length: cantidad }, (_, index) => ({
      nombre:          personas[index]?.nombre          || '',
      rut:             personas[index]?.rut             || '',
      fechaCumpleanos: personas[index]?.fechaCumpleanos || '',
      email:           personas[index]?.email           || '',
      telefono:        personas[index]?.telefono        || '',
    }));
    setPersonas(nuevasPersonas);
  };

  const handlePersonaChange = (index, field, value) => {
    const nuevasPersonas = [...personas];
    nuevasPersonas[index][field] = value;
    setPersonas(nuevasPersonas);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!dia || !diaSalida || !tipoDuracion || !tipoEstancia) {
      alert('Faltan datos de la reserva. Por favor vuelve al inicio.');
      return;
    }

    if (personas.length === 0) {
      alert('Debe ingresar al menos una persona.');
      return;
    }

    // Validación básica de campos obligatorios
    for (let i = 0; i < personas.length; i++) {
      const p = personas[i];
      if (!p.nombre.trim() || !p.rut.trim()) {
        alert(`Complete el nombre y RUT del Huésped ${i + 1}.`);
        return;
      }
    }

    setCargando(true);
    try {
      const participantesConUsuarios = await processParticipants(personas);

      const reserva = {
        checkInDate:  dia,
        checkOutDate: diaSalida,
        stayType:     tipoEstancia,
        roomType:     tipoDuracion,
        cliente:      { id: participantesConUsuarios[0].userId },
        numberOfGuests: cantidadPersonas,
        details: participantesConUsuarios.map((participante) => ({
          guestName: participante.nombre,
          userId:    participante.userId,
        })),
      };

      await confirmReserve(reserva);
      alert('✅ Reserva confirmada exitosamente. ¡Bienvenido al HotelRM!');
      navigate('/');
    } catch (error) {
      console.error('Error al confirmar la reserva:', error);
      
      let errorData = error?.response?.data;
      let errorMsg = 'Error desconocido';

      if (typeof errorData === 'string') {
        errorMsg = errorData;
      } else if (errorData && typeof errorData === 'object') {
        errorMsg = errorData.message || errorData.error || JSON.stringify(errorData);
      } else if (error?.message) {
        errorMsg = error.message;
      }

      alert(`❌ Hubo un error al confirmar la reserva:\n${errorMsg}`);
    } finally {
      setCargando(false);
    }
  };

  return (
    <div>
      <div className="container">
        <h1>Datos de Reserva</h1>
        <form onSubmit={handleSubmit}>

          {/* Resumen de la reserva */}
          <div className="reservation-summary">
            <p><strong>Check-In:</strong>  {dia}</p>
            <p><strong>Check-Out:</strong> {diaSalida}</p>
            <p><strong>Tipo de Estancia:</strong> {STAY_LABELS[tipoEstancia] || tipoEstancia}</p>
            <p><strong>Tipo de Habitación:</strong> {tipoDuracion}</p>
          </div>

          {/* Cantidad de personas */}
          <div>
            <label htmlFor="cantidadPersonas">Cantidad de Huéspedes (1–15):</label>
            <input
              type="number"
              id="cantidadPersonas"
              min="1"
              max="15"
              value={cantidadPersonas}
              onChange={handleCantidadChange}
              required
            />
          </div>

          {/* Formulario por persona */}
          {personas.map((persona, index) => (
            <div key={index} className="persona-form">
              <h3>Huésped {index + 1}</h3>

              <div>
                <label htmlFor={`nombre-${index}`}>Nombre completo:</label>
                <input
                  type="text"
                  id={`nombre-${index}`}
                  value={persona.nombre}
                  onChange={(e) => handlePersonaChange(index, 'nombre', e.target.value)}
                  placeholder="Ej: Juan Pérez"
                  required
                />
              </div>

              <div>
                <label htmlFor={`rut-${index}`}>RUT:</label>
                <input
                  type="text"
                  id={`rut-${index}`}
                  value={persona.rut}
                  onChange={(e) => handlePersonaChange(index, 'rut', e.target.value)}
                  placeholder="Ej: 12.345.678-9"
                  required
                />
              </div>

              <div>
                <label htmlFor={`fechaCumpleanos-${index}`}>Fecha de Nacimiento:</label>
                <input
                  type="date"
                  id={`fechaCumpleanos-${index}`}
                  value={persona.fechaCumpleanos}
                  onChange={(e) => handlePersonaChange(index, 'fechaCumpleanos', e.target.value)}
                />
                <small>* Si hoy es tu cumpleaños, ¡obtienes 50% de descuento!</small>
              </div>

              <div>
                <label htmlFor={`email-${index}`}>Email:</label>
                <input
                  type="email"
                  id={`email-${index}`}
                  value={persona.email}
                  onChange={(e) => handlePersonaChange(index, 'email', e.target.value)}
                  placeholder="correo@ejemplo.com"
                />
              </div>

              <div>
                <label htmlFor={`telefono-${index}`}>Teléfono:</label>
                <input
                  type="text"
                  id={`telefono-${index}`}
                  value={persona.telefono}
                  onChange={(e) => handlePersonaChange(index, 'telefono', e.target.value)}
                  placeholder="Ej: 912345678"
                />
              </div>
            </div>
          ))}

          <button type="submit" disabled={cargando}>
            {cargando ? 'Confirmando reserva...' : 'Confirmar Reserva'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default Formulario;