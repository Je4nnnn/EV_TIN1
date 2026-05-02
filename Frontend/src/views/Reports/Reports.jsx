import { useEffect, useState } from 'react'
import {
  Alert,
  Card,
  CardContent,
  CircularProgress,
  Grid,
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
import { fetchPackageRankingReport, fetchSalesReport } from '../../services/ReportsService'

const currencyFormatter = new Intl.NumberFormat('es-CL', {
  style: 'currency',
  currency: 'CLP',
  maximumFractionDigits: 0,
})

const Reports = () => {
  const [salesReport, setSalesReport] = useState([])
  const [rankingReport, setRankingReport] = useState([])
  const [fechaInicio, setFechaInicio] = useState('2026-01-01')
  const [fechaFin, setFechaFin] = useState('2026-12-31')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    const loadReports = async () => {
      setLoading(true)
      setError('')

      try {
        const [sales, ranking] = await Promise.all([
          fetchSalesReport(fechaInicio, fechaFin),
          fetchPackageRankingReport(fechaInicio, fechaFin),
        ])

        setSalesReport(sales)
        setRankingReport(ranking)
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
        <Typography variant="h4">Reportes comerciales</Typography>
        <Typography variant="body1" color="text.secondary">
          Consulta ventas por periodo y ranking de paquetes vendidos.
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
      {!loading && !error ? (
        <TableContainer component={Paper} elevation={0} sx={{ border: '1px solid rgba(15, 23, 42, 0.08)' }}>
          <Typography variant="h6" sx={{ px: 2, pt: 2, fontWeight: 700 }}>
            Ventas por periodo
          </Typography>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Fecha</TableCell>
                <TableCell>Cliente</TableCell>
                <TableCell>Paquete</TableCell>
                <TableCell align="right">Pasajeros</TableCell>
                <TableCell align="right">Total</TableCell>
                <TableCell align="right">Pagado</TableCell>
                <TableCell>Estado</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {salesReport.map((row) => (
                <TableRow key={`${row.operationDate}-${row.clientName}-${row.packageName}`} hover>
                  <TableCell>{row.operationDate?.slice(0, 10)}</TableCell>
                  <TableCell>{row.clientName}</TableCell>
                  <TableCell>{row.packageName}</TableCell>
                  <TableCell align="right">{row.passengerCount}</TableCell>
                  <TableCell align="right">{currencyFormatter.format(row.reservationTotal || 0)}</TableCell>
                  <TableCell align="right">{currencyFormatter.format(row.paidAmount || 0)}</TableCell>
                  <TableCell>{row.status}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      ) : null}

      {!loading && !error ? (
        <TableContainer component={Paper} elevation={0} sx={{ border: '1px solid rgba(15, 23, 42, 0.08)' }}>
          <Typography variant="h6" sx={{ px: 2, pt: 2, fontWeight: 700 }}>
            Ranking de paquetes
          </Typography>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Paquete</TableCell>
                <TableCell align="right">Reservas</TableCell>
                <TableCell align="right">Pasajeros</TableCell>
                <TableCell align="right">Monto generado</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {rankingReport.map((row) => (
                <TableRow key={row.packageId} hover>
                  <TableCell>{row.packageName}</TableCell>
                  <TableCell align="right">{row.reservationsCount}</TableCell>
                  <TableCell align="right">{row.passengerCount}</TableCell>
                  <TableCell align="right">{currencyFormatter.format(row.totalAmount || 0)}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      ) : null}
    </Stack>
  )
}

export default Reports
