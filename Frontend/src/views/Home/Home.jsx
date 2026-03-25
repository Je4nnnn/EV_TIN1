import React, { useState, useEffect } from 'react';
import { Box, TextField, Button, MenuItem, Typography } from '@mui/material';
import CalendarHome from '../../components/CalendarHome';
import TablePrices from '../../components/TablePrices';
import { useNavigate } from 'react-router-dom';
import './Home.css';
import dayjs from 'dayjs';
import { getReservas } from '../../services/CalendarHomeService'; 

const Home = () => {
  const [selectedDate, setSelectedDate] = useState(null);
  const [selectedTime, setSelectedTime] = useState('');
  const [availableTimes, setAvailableTimes] = useState([]);
  const [selectedDuration, setSelectedDuration] = useState('');
  const [timeRange, setTimeRange] = useState('');
  const [reservas, setReservas] = useState([]);
  const navigate = useNavigate();

  const handleContinue = () => {
    const tipoDuracion =
      selectedDuration === "30"
        ? "10 vueltas"
        : selectedDuration === "35"
        ? "15 vueltas"
        : "20 vueltas";

    navigate('/formulario', {
      state: {
        dia: selectedDate.format('YYYY-MM-DD'),
        horaInicio: selectedTime,
        horaTermino: timeRange.split('-')[1],
        tipoDuracion,
      },
    });
};

const calculateAvailableTimes = () => {
  if (!selectedDate || !selectedDuration) return;

  const isWeekend = [0, 6].includes(selectedDate.day());
  const startHour = isWeekend ? 10 : 14;
  const endHour = 22;

  const durationMinutes = parseInt(selectedDuration);

  const reservasDelDia = Array.isArray(reservas)
    ? reservas.filter((reserva) =>
        dayjs(reserva.fecha_reserva).isSame(selectedDate, 'day')
      )
    : [];

  const horariosConDisponibilidad = [];
  let currentTime = dayjs(selectedDate).hour(startHour).minute(0);

  while (
    currentTime.hour() < endHour ||
    (currentTime.hour() === endHour && currentTime.minute() === 0)
  ) {
    const time = currentTime.format('HH:mm');
    const endTime = currentTime.add(durationMinutes, 'minute');

    if (
      endTime.hour() > endHour ||
      (endTime.hour() === endHour && endTime.minute() > 0)
    ) {
      break;
    }

    const esInvalido = reservasDelDia.some((reserva) => {
      const reservaInicio = dayjs(
        `${reserva.fecha_reserva}T${reserva.hora_inicio}`,
        'YYYY-MM-DDTHH:mm'
      );
      const reservaFin = dayjs(
        `${reserva.fecha_reserva}T${reserva.hora_fin}`,
        'YYYY-MM-DDTHH:mm'
      );
      return (
        (currentTime.isAfter(reservaInicio) || currentTime.isSame(reservaInicio)) &&
        currentTime.isBefore(reservaFin) ||
        (endTime.isAfter(reservaInicio) && endTime.isBefore(reservaFin)) ||
        (currentTime.isBefore(reservaInicio) && endTime.isAfter(reservaFin))
      );
    });

    horariosConDisponibilidad.push({ time, disponible: !esInvalido });
    currentTime = currentTime.add(durationMinutes, 'minute');
  }

  setAvailableTimes(horariosConDisponibilidad);
};

  
  useEffect(() => {
    const fetchReservas = async () => {
      try {
        const reservasData = await getReservas(); 
        setReservas(reservasData);
      } catch (error) {
        console.error('Error al obtener las reservas:', error);
      }
    };

    fetchReservas();
  }, []);

  useEffect(() => {
    calculateAvailableTimes();
  }, [selectedDate, selectedDuration, reservas]);

  useEffect(() => {
    if (selectedTime && selectedDuration) {
      const endTime = dayjs(selectedTime, 'HH:mm')
        .add(parseInt(selectedDuration), 'minute')
        .format('HH:mm');
      setTimeRange(`${selectedTime}-${endTime}`);
    }
  }, [selectedTime, selectedDuration]);

  return (
    <Box className="home-container">
      <Typography variant="h4" className="title">
        Disfruta de la mejor experiencia en Chile - KartingRM
      </Typography>

      <Box display="flex" justifyContent="space-between" alignItems="flex-start" mt={4}>
        <Box flex={2} className="calendar-container" marginRight={2}>
          <Typography variant="h6">Selecciona una fecha:</Typography>
          <CalendarHome
            selectedDate={selectedDate}
            setSelectedDate={setSelectedDate}
            setAvailableTimes={setAvailableTimes}
          />
        </Box>
        <Box flex={1} className="table-prices-container" marginRight={2}>
          <TablePrices />
        </Box>
      </Box>

      {selectedDate && (
        <Box className="time-container" mt={4}>
          <Typography variant="h6">Selecciona la duración de la reserva:</Typography>
          <TextField
            select
            label="Duración"
            value={selectedDuration}
            onChange={(e) => setSelectedDuration(e.target.value)}
            fullWidth
            sx={{ marginBottom: 2 }}
          >
            <MenuItem value="30">10 vueltas o máx 10 min (30 min total)</MenuItem>
            <MenuItem value="35">15 vueltas o máx 15 min (35 min total)</MenuItem>
            <MenuItem value="40">20 vueltas o máx 20 min (40 min total)</MenuItem>
          </TextField>

          {selectedDuration && (
            <>
              <Typography variant="h6">Selecciona un horario:</Typography>
              <TextField
                select
                label="Horario"
                value={selectedTime}
                onChange={(e) => setSelectedTime(e.target.value)}
                fullWidth
              >
                {availableTimes.map(({ time, disponible }) => (
                  <MenuItem key={time} value={time} disabled={!disponible}>
                    {time} {disponible ? "(Disponible)" : "(No disponible)"}
                  </MenuItem>
                ))}
              </TextField>
            </>
          )}

          {selectedTime && (
            <>
              <Typography variant="body1" sx={{ marginTop: 2 }}>
                Horario: {timeRange}
              </Typography>
              <Button
                variant="contained"
                color="primary"
                onClick={handleContinue}
                className="continue-button"
                sx={{ marginTop: 2 }}
              >
                Continuar
              </Button>
            </>
          )}
        </Box>
      )}
    </Box>
  );
};

export default Home;