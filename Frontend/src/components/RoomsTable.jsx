import { useEffect, useState } from 'react'
import dayjs from 'dayjs'
import {
  Alert,
  Chip,
  CircularProgress,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material'
import { getRoomOverview } from '../services/RoomsService'

const occupancyConfig = {
  AVAILABLE: { label: 'Disponible', color: 'success' },
  RESERVED: { label: 'Reservada', color: 'warning' },
  OCCUPIED: { label: 'Ocupada', color: 'error' },
}

export default function RoomsTable() {
  const [rooms, setRooms] = useState([])
  const [referenceDate, setReferenceDate] = useState(dayjs().format('YYYY-MM-DD'))
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    const fetchRooms = async () => {
      setLoading(true)
      setError('')

      try {
        const response = await getRoomOverview(referenceDate)
        setRooms(response)
      } catch (fetchError) {
        setError(fetchError.message)
        setRooms([])
      } finally {
        setLoading(false)
      }
    }

    fetchRooms()
  }, [referenceDate])

  if (loading) {
    return <CircularProgress />
  }

  if (error) {
    return <Alert severity="error">{error}</Alert>
  }

  return (
    <Stack spacing={2}>
      <TextField
        type="date"
        label="Fecha de referencia"
        value={referenceDate}
        onChange={(event) => setReferenceDate(event.target.value)}
        InputLabelProps={{ shrink: true }}
        sx={{ maxWidth: 260 }}
      />
      <TableContainer component={Paper} elevation={0} sx={{ border: '1px solid rgba(15, 23, 42, 0.08)' }}>
        <Typography variant="h6" sx={{ px: 2, pt: 2, fontWeight: 700 }}>
          Estado real de habitaciones
        </Typography>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Numero</TableCell>
              <TableCell>Tipo</TableCell>
              <TableCell>Estado operativo</TableCell>
              <TableCell>Ocupacion</TableCell>
              <TableCell>Reserva asociada</TableCell>
              <TableCell>Rango</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rooms.length > 0 ? (
              rooms.map((room) => {
                const occupancy = occupancyConfig[room.occupancyStatus] || {
                  label: room.occupancyStatus,
                  color: 'default',
                }

                return (
                  <TableRow key={room.id} hover>
                    <TableCell>{room.roomNumber}</TableCell>
                    <TableCell>{room.type}</TableCell>
                    <TableCell>
                      <Chip label={room.operationalStatus} variant="outlined" />
                    </TableCell>
                    <TableCell>
                      <Chip label={occupancy.label} color={occupancy.color} variant="outlined" />
                    </TableCell>
                    <TableCell>{room.reservationCode || '-'}</TableCell>
                    <TableCell>
                      {room.reservedFrom && room.reservedUntil
                        ? `${room.reservedFrom} -> ${room.reservedUntil}`
                        : '-'}
                    </TableCell>
                  </TableRow>
                )
              })
            ) : (
              <TableRow>
                <TableCell colSpan={6} align="center">
                  No hay habitaciones registradas.
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Stack>
  )
}
