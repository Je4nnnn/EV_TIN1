import { useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Grid,
  MenuItem,
  Snackbar,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { confirmReserve, processParticipants } from '../../services/ReservationService'

const initialGuest = { nombre: '', rut: '', fechaCumpleanos: '', email: '', telefono: '' }

const guestFields = [
  { key: 'nombre', label: 'Nombre completo', required: true },
  { key: 'rut', label: 'RUT', required: true },
  { key: 'fechaCumpleanos', label: 'Fecha de nacimiento', type: 'date' },
  { key: 'email', label: 'Correo electronico', type: 'email' },
  { key: 'telefono', label: 'Telefono' },
]

const Formulario = () => {
  const location = useLocation()
  const navigate = useNavigate()
  const { dia, diaSalida, tipoDuracion, tipoEstancia, paqueteTuristico, roomId, roomNumber } = location.state || {}

  const [cantidadPersonas, setCantidadPersonas] = useState(1)
  const [personas, setPersonas] = useState([initialGuest])
  const [feedback, setFeedback] = useState({ type: 'info', message: '' })
  const [submitting, setSubmitting] = useState(false)

  const handleCantidadChange = (event) => {
    const cantidad = Math.max(1, Math.min(15, Number(event.target.value) || 1))
    setCantidadPersonas(cantidad)

    setPersonas(
      Array.from({ length: cantidad }, (_, index) => ({
        ...initialGuest,
        ...personas[index],
      })),
    )
  }

  const handlePersonaChange = (index, field, value) => {
    setPersonas((current) =>
      current.map((persona, currentIndex) =>
        currentIndex === index
          ? {
              ...persona,
              [field]: value,
            }
          : persona,
      ),
    )
  }

  const validateForm = () => {
    if (!dia || !diaSalida || !tipoDuracion || !tipoEstancia || !roomId) {
      return 'Faltan datos base de la reserva. Debes volver al inicio y seleccionar la estadia.'
    }

    for (let index = 0; index < personas.length; index += 1) {
      const persona = personas[index]
      if (!persona.nombre.trim() || !persona.rut.trim()) {
        return `Completa nombre y RUT del huesped ${index + 1}.`
      }
    }

    return ''
  }

  const handleSubmit = async (event) => {
    event.preventDefault()

    const validationMessage = validateForm()
    if (validationMessage) {
      setFeedback({ type: 'error', message: validationMessage })
      return
    }

    setSubmitting(true)
    setFeedback({ type: 'info', message: 'Procesando huespedes y confirmando reserva...' })

    try {
      const participants = await processParticipants(personas)
      const reservePayload = {
        checkInDate: dia,
        checkOutDate: diaSalida,
        stayType: tipoEstancia,
        roomType: tipoDuracion,
        roomId,
        roomNumber,
        touristPackageId: paqueteTuristico?.id || null,
        touristPackageName: paqueteTuristico?.packageName || null,
        cliente: { id: participants[0].userId },
        numberOfGuests: cantidadPersonas,
        details: participants.map((participant) => ({
          guestName: participant.nombre.trim(),
          userId: participant.userId,
        })),
      }

      await confirmReserve(reservePayload)
      setFeedback({ type: 'success', message: 'Reserva confirmada correctamente.' })
      setTimeout(() => navigate('/'), 1200)
    } catch (error) {
      setFeedback({ type: 'error', message: error.message })
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Stack spacing={3}>
      <Box>
        <Typography variant="h4" gutterBottom>
          Confirmacion de reserva
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Completa los datos del huesped principal y acompanantes. El backend valida la informacion final.
        </Typography>
      </Box>

      {feedback.message ? <Alert severity={feedback.type}>{feedback.message}</Alert> : null}

      <Card elevation={0} sx={{ border: '1px solid rgba(15, 23, 42, 0.08)' }}>
        <CardContent>
          <Stack spacing={3} component="form" onSubmit={handleSubmit}>
            <Grid container spacing={2}>
              <Grid item xs={12} md={3}>
                <TextField label="Check-in" value={dia || ''} fullWidth disabled />
              </Grid>
              <Grid item xs={12} md={3}>
                <TextField label="Check-out" value={diaSalida || ''} fullWidth disabled />
              </Grid>
              <Grid item xs={12} md={3}>
                <TextField label="Habitacion" value={tipoDuracion || ''} fullWidth disabled />
              </Grid>
              <Grid item xs={12} md={3}>
                <TextField label="Estancia" value={tipoEstancia || ''} fullWidth disabled />
              </Grid>
              <Grid item xs={12} md={3}>
                <TextField label="Habitacion asignada" value={roomNumber || ''} fullWidth disabled />
              </Grid>
            </Grid>

            {paqueteTuristico ? (
              <Card variant="outlined" sx={{ borderRadius: 3, borderColor: 'rgba(180, 83, 9, 0.3)' }}>
                <CardContent>
                  <Stack spacing={1}>
                    <Typography variant="h6">Reserva desde paquete turistico</Typography>
                    <Typography variant="body1">{paqueteTuristico.packageName}</Typography>
                    <Typography variant="body2" color="text.secondary">
                      {paqueteTuristico.description}
                    </Typography>
                    <Typography variant="body2">
                      Destinos: {paqueteTuristico.destinations?.join(', ')}
                    </Typography>
                    <Typography variant="body2">
                      Actividades: {paqueteTuristico.activities?.join(', ') || 'Sin actividades adicionales'}
                    </Typography>
                    <Typography variant="body2">
                      Servicios extra: {paqueteTuristico.extraServices?.join(', ') || 'Sin extras'}
                    </Typography>
                  </Stack>
                </CardContent>
              </Card>
            ) : null}

            <TextField
              select
              label="Cantidad de huespedes"
              value={cantidadPersonas}
              onChange={handleCantidadChange}
              fullWidth
            >
              {Array.from({ length: 15 }, (_, index) => index + 1).map((count) => (
                <MenuItem key={count} value={count}>
                  {count}
                </MenuItem>
              ))}
            </TextField>

            {personas.map((persona, index) => (
              <Card
                key={`guest-${index}`}
                variant="outlined"
                sx={{ borderRadius: 3, borderColor: 'rgba(15, 118, 110, 0.24)' }}
              >
                <CardContent>
                  <Stack spacing={2}>
                    <Typography variant="h6">Huesped {index + 1}</Typography>
                    <Grid container spacing={2}>
                      {guestFields.map((field) => (
                        <Grid item xs={12} md={field.key === 'nombre' ? 6 : 3} key={field.key}>
                          <TextField
                            type={field.type || 'text'}
                            label={field.label}
                            value={persona[field.key] || ''}
                            onChange={(event) => handlePersonaChange(index, field.key, event.target.value)}
                            required={Boolean(field.required)}
                            fullWidth
                            InputLabelProps={field.type === 'date' ? { shrink: true } : undefined}
                          />
                        </Grid>
                      ))}
                    </Grid>
                  </Stack>
                </CardContent>
              </Card>
            ))}

            <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
              <Button variant="outlined" onClick={() => navigate('/')}>
                Volver
              </Button>
              <Button type="submit" variant="contained" disabled={submitting}>
                {submitting ? 'Confirmando...' : 'Confirmar reserva'}
              </Button>
            </Box>
          </Stack>
        </CardContent>
      </Card>

      <Snackbar
        open={feedback.type === 'success'}
        autoHideDuration={2500}
        onClose={() => setFeedback((current) => ({ ...current, message: '' }))}
      >
        <Alert severity="success" variant="filled">
          {feedback.message}
        </Alert>
      </Snackbar>
    </Stack>
  )
}

export default Formulario
