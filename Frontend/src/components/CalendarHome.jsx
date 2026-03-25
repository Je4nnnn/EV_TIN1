import React from 'react';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { TextField } from '@mui/material';

export default function CalendarHome({ selectedDate, setSelectedDate }) {
  return (
    <LocalizationProvider dateAdapter={AdapterDayjs}>
      <DatePicker
        label="Selecciona un día"
        value={selectedDate}
        onChange={(newDate) => setSelectedDate(newDate)}
        renderInput={(params) => <TextField {...params} />}
      />
    </LocalizationProvider>
  );
}