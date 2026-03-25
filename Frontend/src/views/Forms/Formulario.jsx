import React, { useState } from 'react';
import { useLocation } from 'react-router-dom';
import { processParticipants, confirmReserve, getUserByRut, createUser } from '../../services/ReserveService';
import './Formulario.css';

const Formulario = () => {
  const location = useLocation();
  const { dia, horaInicio, horaTermino, tipoDuracion } = location.state || {};

  const [cantidadPersonas, setCantidadPersonas] = useState(1);
  const [personas, setPersonas] = useState([]); 

  const handleCantidadChange = (e) => {
    const cantidad = parseInt(e.target.value);
    setCantidadPersonas(cantidad);

    const nuevasPersonas = Array.from({ length: cantidad }, (_, index) => ({
      nombre: personas[index]?.nombre || '',
      rut: personas[index]?.rut || '',
      fechaCumpleanos: personas[index]?.fechaCumpleanos || '',
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
  
    try {
      if (personas.length === 0) {
        alert('Debe ingresar al menos una persona.');
        return;
      }
  
      const participantesConUsuarios = await processParticipants(personas);

      const reserva = {
        fecha_uso: dia,
        hora_inicio: horaInicio,
        hora_fin: horaTermino,
        vueltas_o_tiempo: tipoDuracion,
        cliente: { id: participantesConUsuarios[0].userId }, 
        cantidad_personas: cantidadPersonas,
        detalles: participantesConUsuarios.map((participante) => ({
          memberName: participante.nombre,
          rut: participante.rut,
          dateBirthday: participante.fechaCumpleanos,
          userId: participante.userId,
        })),
      };
  
      const response = await confirmReserve(reserva);
      alert('Reserva confirmada exitosamente');
    } catch (error) {
      console.error('Error al confirmar la reserva:', error);
      alert('Hubo un error al confirmar la reserva');
    }
  };

  return (
    <div>
      <div className="container">
        <h1>Datos de reserva</h1>
        <form onSubmit={handleSubmit}>
          <p><strong>Día:</strong> {dia}</p>
          <p><strong>Hora de Inicio:</strong> {horaInicio}</p>
          <p><strong>Hora de Término:</strong> {horaTermino}</p>
          <p><strong>Tipo de Duración:</strong> {tipoDuracion}</p>

          <div>
            <label htmlFor="cantidadPersonas">Cantidad de Personas (1-15):</label>
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

          {personas.map((persona, index) => (
  <div key={index} className="persona-form">
    <h3>Persona {index + 1}</h3>
    <div>
      <label htmlFor={`nombre-${index}`}>Nombre:</label>
      <input
        type="text"
        id={`nombre-${index}`}
        value={persona.nombre}
        onChange={(e) => handlePersonaChange(index, 'nombre', e.target.value)}
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
        required
      />
    </div>
    <div>
      <label htmlFor={`fechaCumpleanos-${index}`}>Fecha de Cumpleaños:</label>
      <input
        type="date"
        id={`fechaCumpleanos-${index}`}
        value={persona.fechaCumpleanos}
        onChange={(e) => handlePersonaChange(index, 'fechaCumpleanos', e.target.value)}
        required
      />
    </div>
    <div>
      <label htmlFor={`email-${index}`}>Email:</label>
      <input
        type="email"
        id={`email-${index}`}
        value={persona.email || ''}
        onChange={(e) => handlePersonaChange(index, 'email', e.target.value)}
        required
      />
    </div>
    <div>
      <label htmlFor={`telefono-${index}`}>Teléfono:</label>
      <input
        type="text"
        id={`telefono-${index}`}
        value={persona.telefono || ''}
        onChange={(e) => handlePersonaChange(index, 'telefono', e.target.value)}
        required
      />
    </div>
  </div>
))}

          <button type="submit">Confirmar Reserva</button>
        </form>
      </div>
    </div>
  );
};

export default Formulario;