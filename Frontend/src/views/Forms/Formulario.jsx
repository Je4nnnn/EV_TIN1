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
import { useAuth } from '../../auth/useAuth'

const initialGuest = { nombre: '', rut: '', fechaCumpleanos: '', email: '', telefono: '' }
const MAX_REASONABLE_AGE = 100
const MIN_BOOKING_AGE = 18
const EMAIL_REGEX = /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i
const PHONE_REGEX = /^\+?[0-9 ]{8,15}$/

const guestFields = [
  { key: 'nombre', label: 'Nombre completo', required: true },
  { key: 'rut', label: 'RUT', required: true },
  { key: 'fechaCumpleanos', label: 'Fecha de nacimiento', type: 'date', required: true },
  { key: 'email', label: 'Correo electronico', type: 'email', required: true },
  { key: 'telefono', label: 'Telefono' },
]

const cleanRut = (rut) => (rut ? rut.replace(/[.-]/g, '').trim().toUpperCase() : '')

const isValidRut = (rut) => {
  const normalizedRut = cleanRut(rut)
  if (normalizedRut.length < 2) {
    return false
  }

  const numberPart = normalizedRut.slice(0, -1)
  const verifier = normalizedRut.slice(-1)
  if (!/^\d+$/.test(numberPart)) {
    return false
  }

  let sum = 0
  let multiplier = 2

  for (let index = numberPart.length - 1; index >= 0; index -= 1) {
    sum += Number(numberPart[index]) * multiplier
    multiplier = multiplier === 7 ? 2 : multiplier + 1
  }

  const remainder = 11 - (sum % 11)
  const expectedVerifier = remainder === 11 ? '0' : remainder === 10 ? 'K' : String(remainder)
  return expectedVerifier === verifier
}

const calculateAgeOnDate = (birthDate, referenceDate) => {
  if (!birthDate || !referenceDate) {
    return null
  }

  const birth = new Date(`${birthDate}T00:00:00`)
  const reference = new Date(`${referenceDate}T00:00:00`)
  if (Number.isNaN(birth.getTime()) || Number.isNaN(reference.getTime())) {
    return null
  }

  let age = reference.getFullYear() - birth.getFullYear()
  const monthDifference = reference.getMonth() - birth.getMonth()
  const dayDifference = reference.getDate() - birth.getDate()

  if (monthDifference < 0 || (monthDifference === 0 && dayDifference < 0)) {
    age -= 1
  }

  return age
}

const Formulario = () => {
  const location = useLocation()
  const navigate = useNavigate()
  const { dia, diaSalida, tipoDuracion, tipoEstancia, paqueteTuristico, roomId, roomNumber } = location.state || {}
  const auth = useAuth()

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

    const seenRuts = new Set()

    for (let index = 0; index < personas.length; index += 1) {
      const persona = personas[index]
      const normalizedRut = cleanRut(persona.rut)

      if (!persona.nombre.trim() || !persona.rut.trim() || !persona.fechaCumpleanos) {
        return `Completa nombre, RUT y fecha de nacimiento del huesped ${index + 1}.`
      }

      if (persona.nombre.trim().length < 3) {
        return `El nombre del huesped ${index + 1} debe tener al menos 3 caracteres.`
      }

      if (!isValidRut(normalizedRut)) {
        return `El RUT del huesped ${index + 1} no es valido.`
      }

      if (seenRuts.has(normalizedRut)) {
        return 'No puedes repetir el mismo RUT dentro de una reserva.'
      }
      seenRuts.add(normalizedRut)

      if (!persona.email?.trim()) {
        return `El correo del huesped ${index + 1} es obligatorio.`
      }

      if (!EMAIL_REGEX.test(persona.email.trim())) {
        return `El correo del huesped ${index + 1} no es valido.`
      }

      if (persona.telefono?.trim() && !PHONE_REGEX.test(persona.telefono.trim())) {
        return `El telefono del huesped ${index + 1} no es valido.`
      }

      const ageAtCheckIn = calculateAgeOnDate(persona.fechaCumpleanos, dia)
      if (ageAtCheckIn === null) {
        return `La fecha de nacimiento del huesped ${index + 1} no es valida.`
      }

      if (ageAtCheckIn < 0) {
        return `La fecha de nacimiento del huesped ${index + 1} no puede ser posterior al check-in.`
      }

      if (ageAtCheckIn > MAX_REASONABLE_AGE) {
        return `No se permiten huespedes mayores de ${MAX_REASONABLE_AGE} anos.`
      }

      if (index === 0 && ageAtCheckIn < MIN_BOOKING_AGE) {
        return 'El huesped principal debe ser mayor de edad para realizar la reserva.'
      }
    }

    return ''
  }

  const handleSubmit = async (event) => {
    event.preventDefault()

    if (auth.enabled && !auth.authenticated) {
      setFeedback({ type: 'error', message: 'Debes iniciar sesion para registrar una reserva.' })
      return
    }

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

      const reservation = await confirmReserve(reservePayload)
      setFeedback({ type: 'success', message: 'Reserva creada. Continua con el pago simulado.' })
      setTimeout(() => navigate('/payment', { state: { reservation } }), 900)
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
          Completa los datos del cliente y acompanantes. La reserva quedara pendiente hasta pagar el monto total.
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
                    <Typography variant="body2">
                      Precio original estimado: ${Number((paqueteTuristico.price || 0) * cantidadPersonas).toLocaleString('es-CL')}
                    </Typography>
                    <Typography variant="body2">
                      Descuento estimado por grupo: {cantidadPersonas >= 4 ? '10%' : '0%'}
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
                    {index === 0 ? (
                      <Typography variant="body2" color="text.secondary">
                        Este huesped se registra como titular de la reserva y debe ser mayor de edad.
                      </Typography>
                    ) : null}
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
                            inputProps={field.type === 'date' ? { max: dia || diaSalida || undefined } : undefined}
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
