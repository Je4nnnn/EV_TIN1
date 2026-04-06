import React, { useState, useEffect } from 'react';
import { Box, TextField, Button, MenuItem, Typography, Alert } from '@mui/material';
import CalendarHome from '../../components/CalendarHome';
import TablePrices from '../../components/TablePrices';
import { useNavigate } from 'react-router-dom';
import './Home.css';
import dayjs from 'dayjs';
import { getReservas } from '../../services/CalendarHomeService';

/**
 * Tipos de estancia en el hotel:
 * - "Mañana"  : turno diurno (ej: 08:00 - 18:00)
 * - "Noche"   : turno nocturno (ej: 18:00 - 08:00 del día siguiente)
 * - "Completo": día + noche completo (24h)
 */
const STAY_TYPES = [
  { value: 'Mañana',   label: 'Turno Mañana (08:00 – 18:00)',        checkoutDays: 0 },
  { value: 'Noche',    label: 'Turno Noche (18:00 – 08:00)',          checkoutDays: 1 },
  { value: 'Completo', label: 'Día Completo (Check-in hoy / out mañana)', checkoutDays: 1 },
];

const ROOM_TYPES = [
  { value: 'Simple', label: 'Habitación Simple' },
  { value: 'Double', label: 'Habitación Doble' },
  { value: 'Suite',  label: 'Habitación Suite' },
];

const Home = () => {
  const [checkInDate, setCheckInDate]     = useState(null);
  const [checkOutDate, setCheckOutDate]   = useState(null);
  const [stayType, setStayType]           = useState('');
  const [roomType, setRoomType]           = useState('');
  const [reservas, setReservas]           = useState([]);
  const [conflicto, setConflicto]         = useState(false);
  const navigate = useNavigate();

  // Cargar reservas existentes para mostrar disponibilidad
  useEffect(() => {
    const fetchReservas = async () => {
      try {
        const data = await getReservas();
        setReservas(data);
      } catch (error) {
        console.error('Error al obtener reservas:', error);
      }
    };
    fetchReservas();
  }, []);

  // Calcular checkOutDate automáticamente según el tipo de estancia
  useEffect(() => {
    if (checkInDate && stayType) {
      const tipoSeleccionado = STAY_TYPES.find(t => t.value === stayType);
      if (tipoSeleccionado) {
        const checkout = checkInDate.add(tipoSeleccionado.checkoutDays, 'day');
        setCheckOutDate(checkout);
        verificarConflicto(checkInDate, checkout, stayType);
      }
    }
  }, [checkInDate, stayType, reservas]);

  // Si el usuario cambia el número de noches manualmente (para estadías multi-noche)
  const handleCheckOutChange = (newDate) => {
    setCheckOutDate(newDate);
    if (checkInDate && stayType) {
      verificarConflicto(checkInDate, newDate, stayType);
    }
  };

  const verificarConflicto = (checkIn, checkOut, tipo) => {
    const hay = reservas.some((reserva) => {
      if (reserva.stayType !== tipo) return false;
      const resIn  = dayjs(reserva.checkInDate);
      const resOut = dayjs(reserva.checkOutDate);
      // Hay conflicto si los rangos se solapan
      return checkIn.isBefore(resOut) && checkOut.isAfter(resIn);
    });
    setConflicto(hay);
  };

  const handleContinue = () => {
    if (!checkInDate || !checkOutDate || !stayType || !roomType) {
      alert('Por favor completa todos los campos antes de continuar.');
      return;
    }
    if (conflicto) {
      alert('Ya existe una reserva en ese periodo. Por favor elige otro turno o fecha.');
      return;
    }

    navigate('/formulario', {
      state: {
        dia:          checkInDate.format('YYYY-MM-DD'),
        diaSalida:    checkOutDate.format('YYYY-MM-DD'),
        tipoDuracion: roomType,
        tipoEstancia: stayType,
      },
    });
  };

  const isFormValid = checkInDate && checkOutDate && stayType && roomType && !conflicto;

  return (
    <Box className="home-container">
      <Typography variant="h4" className="title">
        Bienvenido a HotelRM – Reserva tu habitación
      </Typography>

      <Box display="flex" justifyContent="space-between" alignItems="flex-start" mt={4}>
        {/* Calendario para seleccionar Check-In */}
        <Box flex={2} className="calendar-container" marginRight={2}>
          <Typography variant="h6">Selecciona la fecha de Check-In:</Typography>
          <CalendarHome
            selectedDate={checkInDate}
            setSelectedDate={setCheckInDate}
            setAvailableTimes={() => {}}
          />
        </Box>

        {/* Tabla de precios */}
        <Box flex={1} className="table-prices-container" marginRight={2}>
          <TablePrices />
        </Box>
      </Box>

      {checkInDate && (
        <Box className="time-container" mt={4}>
          {/* Tipo de habitación */}
          <Typography variant="h6" sx={{ mb: 1 }}>Tipo de Habitación:</Typography>
          <TextField
            select
            label="Tipo de Habitación"
            value={roomType}
            onChange={(e) => setRoomType(e.target.value)}
            fullWidth
            sx={{ marginBottom: 2 }}
          >
            {ROOM_TYPES.map((r) => (
              <MenuItem key={r.value} value={r.value}>{r.label}</MenuItem>
            ))}
          </TextField>

          {/* Tipo de estancia */}
          <Typography variant="h6" sx={{ mb: 1 }}>Tipo de Estancia:</Typography>
          <TextField
            select
            label="Tipo de Estancia"
            value={stayType}
            onChange={(e) => setStayType(e.target.value)}
            fullWidth
            sx={{ marginBottom: 2 }}
          >
            {STAY_TYPES.map((s) => (
              <MenuItem key={s.value} value={s.value}>{s.label}</MenuItem>
            ))}
          </TextField>

          {/* Fechas calculadas */}
          {stayType && checkOutDate && (
            <Box sx={{ mb: 2 }}>
              <Typography variant="body1">
                <strong>Check-In:</strong> {checkInDate.format('DD/MM/YYYY')}
              </Typography>
              <Typography variant="body1">
                <strong>Check-Out:</strong> {checkOutDate.format('DD/MM/YYYY')}
              </Typography>

              {/* Para estadías multi-noche, permitir ajustar el checkout */}
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                ¿Estadía de más de una noche? Ajusta la fecha de salida:
              </Typography>
              <TextField
                type="date"
                label="Fecha de Check-Out"
                value={checkOutDate.format('YYYY-MM-DD')}
                inputProps={{ min: checkInDate.add(1, 'day').format('YYYY-MM-DD') }}
                onChange={(e) => handleCheckOutChange(dayjs(e.target.value))}
                sx={{ mt: 1 }}
                InputLabelProps={{ shrink: true }}
              />
            </Box>
          )}

          {/* Alerta de conflicto */}
          {conflicto && (
            <Alert severity="warning" sx={{ mb: 2 }}>
              Ya existe una reserva en ese periodo. Por favor selecciona otra fecha o tipo de estancia.
            </Alert>
          )}

          {/* Botón continuar */}
          {stayType && roomType && (
            <Button
              variant="contained"
              color="primary"
              onClick={handleContinue}
              disabled={!isFormValid}
              sx={{ marginTop: 1 }}
            >
              Continuar con la Reserva
            </Button>
          )}
        </Box>
      )}
    </Box>
  );
};

export default Home;