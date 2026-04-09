import dayjs from 'dayjs'
import {
  Box,
  Chip,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material'

const buildWeekDays = (referenceDate) => {
  const start = dayjs(referenceDate).startOf('week').add(1, 'day')
  return Array.from({ length: 7 }, (_, index) => start.add(index, 'day'))
}

const isReservationOnDay = (reservation, day) => {
  const checkIn = dayjs(reservation.checkInDate)
  const checkOut = dayjs(reservation.checkOutDate)
  return !day.isBefore(checkIn, 'day') && !day.isAfter(checkOut, 'day')
}

const RackBoard = ({ reservations, referenceDate }) => {
  const weekDays = buildWeekDays(referenceDate)
  const rooms = [...new Set(reservations.map((reservation) => reservation.roomNumber).filter(Boolean))].sort()

  return (
    <TableContainer component={Paper} elevation={0} sx={{ border: '1px solid rgba(15, 23, 42, 0.08)' }}>
      <Table sx={{ minWidth: 1200 }}>
        <TableHead>
          <TableRow>
            <TableCell sx={{ minWidth: 120, fontWeight: 700 }}>Habitacion</TableCell>
            {weekDays.map((day) => (
              <TableCell key={day.format('YYYY-MM-DD')} sx={{ minWidth: 180 }}>
                <Stack spacing={0.3}>
                  <Typography variant="subtitle2">{day.format('dddd')}</Typography>
                  <Typography variant="caption" color="text.secondary">
                    {day.format('DD/MM/YYYY')}
                  </Typography>
                </Stack>
              </TableCell>
            ))}
          </TableRow>
        </TableHead>
        <TableBody>
          {rooms.map((roomNumber) => (
            <TableRow key={roomNumber} hover>
              <TableCell sx={{ fontWeight: 700 }}>{roomNumber}</TableCell>
              {weekDays.map((day) => {
                const reservation = reservations.find(
                  (item) => item.roomNumber === roomNumber && isReservationOnDay(item, day),
                )

                return (
                  <TableCell
                    key={`${roomNumber}-${day.format('YYYY-MM-DD')}`}
                    sx={{
                      backgroundColor: reservation ? 'rgba(15, 118, 110, 0.12)' : 'rgba(148, 163, 184, 0.08)',
                      verticalAlign: 'top',
                    }}
                  >
                    {reservation ? (
                      <Stack spacing={1}>
                        <Chip size="small" color="primary" label={reservation.roomType} />
                        <Typography variant="body2" sx={{ fontWeight: 600 }}>
                          {reservation.reservationCode}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          {reservation.stayType}
                        </Typography>
                        {reservation.touristPackageName ? (
                          <Typography variant="caption">{reservation.touristPackageName}</Typography>
                        ) : null}
                      </Stack>
                    ) : (
                      <Typography variant="caption" color="text.secondary">
                        Disponible
                      </Typography>
                    )}
                  </TableCell>
                )
              })}
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  )
}

export default RackBoard
