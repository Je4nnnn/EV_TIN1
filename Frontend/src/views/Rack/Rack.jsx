import React, { useEffect, useState } from 'react';
import WeeklyCalendar from '../../components/Rack';
import './rack.css';
import { getReserves } from '../../services/ReserveService';

const RackView = () => {
  const [events, setEvents] = useState([]);

  useEffect(() => {
    const fetchReservations = async () => {
      try {
        const reservas = await getReserves();
        console.log('Reservas obtenidas:', reservas); 
  
        const formattedEvents = reservas.map((reserva) => ({
          title: 'Reserva',
          start: `${reserva.fecha_reserva}T${reserva.hora_inicio}`,
          end: `${reserva.fecha_reserva}T${reserva.hora_fin}`,
          codigo_reserva: reserva.codigo_reserva, 
        }));
  
        console.log('Eventos formateados:', formattedEvents); 
        setEvents(formattedEvents);
      } catch (error) {
        console.error('Error al obtener las reservas:', error);
      }
    };
  
    fetchReservations();
  }, []);

  return (
    <div className="rack-page">
      <WeeklyCalendar events={events} />
    </div>
  );
};

export default RackView;