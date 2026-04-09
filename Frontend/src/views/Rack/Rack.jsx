import React, { useEffect, useState } from 'react'
import dayjs from 'dayjs'
import {
  Alert,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import RackBoard from '../../components/Rack'
import './Rack.css'
import { deleteReservation, getReserves } from '../../services/ReservationService'

const RackView = () => {
  const [reservations, setReservations] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [feedback, setFeedback] = useState('')
  const [cancellingId, setCancellingId] = useState(null)
  const [referenceDate, setReferenceDate] = useState(dayjs().format('YYYY-MM-DD'))

  const fetchReservations = async () => {
    setLoading(true)
    setError('')

    try {
      const reservas = await getReserves()
      setReservations(reservas)
    } catch (fetchError) {
      setError(fetchError.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchReservations()
  }, [])

  const handleCancelReservation = async (reservation) => {
    const confirmed = window.confirm(
      `Se cancelara la reserva ${reservation.reservationCode} de la habitacion ${reservation.roomNumber}.`,
    )

    if (!confirmed) {
      return
    }

    setCancellingId(reservation.id)
    setError('')

    try {
      await deleteReservation(reservation.id)
      setFeedback(`La reserva ${reservation.reservationCode} fue cancelada, archivada y la habitacion quedo disponible.`)
      await fetchReservations()
    } catch (cancelError) {
      setError(cancelError.message)
    } finally {
      setCancellingId(null)
    }
  }

  const activeReservations = reservations
    .filter((reservation) => reservation.checkOutDate && !dayjs(reservation.checkOutDate).isBefore(dayjs(), 'day'))
    .sort((left, right) => dayjs(left.checkInDate).valueOf() - dayjs(right.checkInDate).valueOf())

  return (
    <Stack spacing={3} className="rack-page">
      <div>
        <Typography variant="h4" gutterBottom>
          Rack semanal por habitacion
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Visualiza la ocupacion del hotel por habitacion y por dia, con lectura directa de disponibilidad.
        </Typography>
      </div>

      <TextField
        type="date"
        label="Semana de referencia"
        value={referenceDate}
        onChange={(event) => setReferenceDate(event.target.value)}
        InputLabelProps={{ shrink: true }}
        sx={{ maxWidth: 260 }}
      />

      {loading ? <CircularProgress /> : null}
      {error ? <Alert severity="error">{error}</Alert> : null}
      {feedback ? <Alert severity="success">{feedback}</Alert> : null}
      {!loading && !error ? <RackBoard reservations={reservations} referenceDate={referenceDate} /> : null}
      {!loading && !error ? (
        <Stack spacing={2}>
          <Typography variant="h5">Reservas activas y cancelacion</Typography>
          <Typography variant="body2" color="text.secondary">
            Puedes cancelar una reserva para liberar inmediatamente la habitacion. La informacion queda archivada en la base de datos.
          </Typography>
          {activeReservations.length > 0 ? (
            activeReservations.map((reservation) => (
              <Card key={reservation.id} elevation={0} sx={{ border: '1px solid rgba(15, 23, 42, 0.08)' }}>
                <CardContent>
                  <Stack
                    direction={{ xs: 'column', md: 'row' }}
                    spacing={2}
                    justifyContent="space-between"
                    alignItems={{ xs: 'flex-start', md: 'center' }}
                  >
                    <Stack spacing={0.6}>
                      <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>
                        {reservation.reservationCode} - {reservation.roomNumber}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Titular: {reservation.cliente?.name || 'Cliente'} | RUT: {reservation.cliente?.rut || '-'}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        {reservation.checkInDate} al {reservation.checkOutDate} | {reservation.stayType}
                      </Typography>
                      <Stack direction="row" spacing={1} flexWrap="wrap">
                        <Chip size="small" label={reservation.roomType} color="primary" />
                        {reservation.touristPackageName ? (
                          <Chip size="small" label={reservation.touristPackageName} variant="outlined" />
                        ) : null}
                      </Stack>
                    </Stack>
                    <Button
                      color="error"
                      variant="outlined"
                      onClick={() => handleCancelReservation(reservation)}
                      disabled={cancellingId === reservation.id}
                    >
                      {cancellingId === reservation.id ? 'Cancelando...' : 'Cancelar reserva'}
                    </Button>
                  </Stack>
                </CardContent>
              </Card>
            ))
          ) : (
            <Alert severity="info">No hay reservas activas para cancelar.</Alert>
          )}
        </Stack>
      ) : null}
    </Stack>
  )
}

export default RackView
