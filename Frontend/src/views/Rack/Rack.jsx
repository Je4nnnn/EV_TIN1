import React, { useEffect, useState } from 'react';
import WeeklyCalendar from '../../components/Rack';
import './Rack.css';
import { getReserves } from '../../services/ReservationService';

// Horas de referencia por tipo de estancia
const STAY_HOURS = {
  Mañana:   { start: '08:00', end: '18:00' },
  Noche:    { start: '18:00', end: '08:00' },
  Completo: { start: '08:00', end: '08:00' },
};

const RackView = () => {
  const [events, setEvents] = useState([]);

  useEffect(() => {
    const fetchReservations = async () => {
      try {
        const reservas = await getReserves();
        console.log('Reservas obtenidas:', reservas);

        const formattedEvents = reservas
          .filter((reserva) => reserva.checkInDate)
          .map((reserva) => {
            const horas = STAY_HOURS[reserva.stayType] || { start: '12:00', end: '12:00' };
            const checkIn  = reserva.checkInDate;
            // Para turno "Noche" y "Completo", el checkout es al día siguiente
            const checkOut = reserva.checkOutDate || checkIn;

            return {
              title: `${reserva.roomType || 'Habita.'} – ${reserva.stayType || ''} [${reserva.reservationCode || ''}]`,
              start: `${checkIn}T${horas.start}`,
              end:   `${checkOut}T${horas.end}`,
              reservationCode: reserva.reservationCode,
            };
          });

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