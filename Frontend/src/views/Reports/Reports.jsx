import { useEffect, useState } from 'react'
import {
  Alert,
  Card,
  CardContent,
  CircularProgress,
  Grid,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { fetchReporteIngresos, fetchReporteParticipantes } from '../../services/ReportsService'
import ReportTable from '../../components/ReportTable'

const Reports = () => {
  const [reporte, setReporte] = useState(null)
  const [reporteParticipantes, setReporteParticipantes] = useState(null)
  const [fechaInicio, setFechaInicio] = useState('2026-01-01')
  const [fechaFin, setFechaFin] = useState('2026-12-31')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    const loadReports = async () => {
      setLoading(true)
      setError('')

      try {
        const [incomeReport, participantsReport] = await Promise.all([
          fetchReporteIngresos(fechaInicio, fechaFin),
          fetchReporteParticipantes(fechaInicio, fechaFin),
        ])

        setReporte(incomeReport)
        setReporteParticipantes(participantsReport)
      } catch (reportError) {
        setError(reportError.message)
      } finally {
        setLoading(false)
      }
    }

    loadReports()
  }, [fechaInicio, fechaFin])

  return (
    <Stack spacing={3}>
      <Stack spacing={1}>
        <Typography variant="h4">Reportes operativos</Typography>
        <Typography variant="body1" color="text.secondary">
          Consulta ingresos consolidados por tipo de habitacion y por volumen de huespedes.
        </Typography>
      </Stack>

      <Card elevation={0} sx={{ border: '1px solid rgba(15, 23, 42, 0.08)' }}>
        <CardContent>
          <Grid container spacing={2}>
            <Grid item xs={12} md={4}>
              <TextField
                type="date"
                label="Fecha inicio"
                value={fechaInicio}
                onChange={(event) => setFechaInicio(event.target.value)}
                InputLabelProps={{ shrink: true }}
                fullWidth
              />
            </Grid>
            <Grid item xs={12} md={4}>
              <TextField
                type="date"
                label="Fecha fin"
                value={fechaFin}
                onChange={(event) => setFechaFin(event.target.value)}
                InputLabelProps={{ shrink: true }}
                fullWidth
              />
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {loading ? <CircularProgress /> : null}
      {error ? <Alert severity="error">{error}</Alert> : null}
      {!loading && !error && reporte ? <ReportTable reporte={reporte} title="Ingresos por tipo de habitacion" /> : null}
      {!loading && !error && reporteParticipantes ? (
        <ReportTable reporte={reporteParticipantes} title="Ingresos por cantidad de huespedes" />
      ) : null}
    </Stack>
  )
}

export default Reports
