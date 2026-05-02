import { useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Grid,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { simulatePayment } from '../../services/PaymentService'
import { useAuth } from '../../auth/useAuth'

const currencyFormatter = new Intl.NumberFormat('es-CL', {
  style: 'currency',
  currency: 'CLP',
  maximumFractionDigits: 0,
})

const Payment = () => {
  const location = useLocation()
  const navigate = useNavigate()
  const auth = useAuth()
  const reservation = location.state?.reservation

  const [cardNumber, setCardNumber] = useState('4111111111111111')
  const [expirationDate, setExpirationDate] = useState('12/30')
  const [cvv, setCvv] = useState('123')
  const [feedback, setFeedback] = useState({ type: '', message: '' })
  const [submitting, setSubmitting] = useState(false)

  const handlePayment = async (event) => {
    event.preventDefault()

    if (!reservation?.id) {
      setFeedback({ type: 'error', message: 'No hay una reserva pendiente para pagar.' })
      return
    }

    if (!cardNumber.trim() || !expirationDate.trim() || !cvv.trim()) {
      setFeedback({ type: 'error', message: 'Completa los datos de tarjeta simulada.' })
      return
    }

    setSubmitting(true)
    try {
      await simulatePayment({
        reservationId: reservation.id,
        amount: reservation.finalAmount,
        cardNumber,
        expirationDate,
        cvv,
      })
      setFeedback({ type: 'success', message: 'Pago simulado aprobado. Reserva confirmada.' })
      setTimeout(() => navigate('/home'), 1400)
    } catch (error) {
      setFeedback({ type: 'error', message: error.message })
    } finally {
      setSubmitting(false)
    }
  }

  if (auth.enabled && auth.initialized && !auth.authenticated) {
    return (
      <Stack spacing={2} sx={{ py: 6, maxWidth: 560 }}>
        <Typography variant="h4">Pago protegido</Typography>
        <Alert severity="info">Debes iniciar sesion para pagar una reserva.</Alert>
        <Box>
          <Button variant="contained" onClick={() => auth.login()}>
            Iniciar sesion
          </Button>
        </Box>
      </Stack>
    )
  }

  return (
    <Stack spacing={3} sx={{ maxWidth: 860 }}>
      <Box>
        <Typography variant="h4" gutterBottom>
          Pago simulado
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Revisa el monto total y confirma el pago completo con tarjeta de credito simulada.
        </Typography>
      </Box>

      {feedback.message ? <Alert severity={feedback.type}>{feedback.message}</Alert> : null}

      <Card elevation={0} sx={{ border: '1px solid rgba(15, 23, 42, 0.08)' }}>
        <CardContent>
          <Stack spacing={2.5} component="form" onSubmit={handlePayment}>
            <Grid container spacing={2}>
              <Grid item xs={12} md={6}>
                <TextField label="Reserva" value={reservation?.reservationCode || reservation?.id || ''} fullWidth disabled />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField label="Estado" value={reservation?.status || 'PENDING_PAYMENT'} fullWidth disabled />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  label="Precio original"
                  value={currencyFormatter.format(reservation?.originalAmount || reservation?.finalAmount || 0)}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  label="Monto a pagar"
                  value={currencyFormatter.format(reservation?.finalAmount || 0)}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  label="Descuentos aplicados"
                  value={reservation?.discountBreakdown || 'sin descuentos aplicados'}
                  fullWidth
                  disabled
                />
              </Grid>
              <Grid item xs={12} md={5}>
                <TextField
                  label="Numero de tarjeta"
                  value={cardNumber}
                  onChange={(event) => setCardNumber(event.target.value)}
                  fullWidth
                />
              </Grid>
              <Grid item xs={12} md={3}>
                <TextField
                  label="Expiracion"
                  value={expirationDate}
                  onChange={(event) => setExpirationDate(event.target.value)}
                  fullWidth
                />
              </Grid>
              <Grid item xs={12} md={2}>
                <TextField label="CVV" value={cvv} onChange={(event) => setCvv(event.target.value)} fullWidth />
              </Grid>
            </Grid>

            <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2 }}>
              <Button variant="outlined" onClick={() => navigate('/home')}>
                Cancelar
              </Button>
              <Button type="submit" variant="contained" disabled={submitting || !reservation?.id}>
                {submitting ? 'Procesando...' : 'Confirmar pago'}
              </Button>
            </Box>
          </Stack>
        </CardContent>
      </Card>
    </Stack>
  )
}

export default Payment
