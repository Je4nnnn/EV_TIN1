import {
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material'

const currencyFormatter = new Intl.NumberFormat('es-CL', {
  style: 'currency',
  currency: 'CLP',
  maximumFractionDigits: 0,
})

const ReportTable = ({ reporte, title }) => {
  const categorias = Object.keys(reporte).filter((key) => key !== 'TOTAL')
  const meses = Object.keys(reporte.TOTAL || {}).filter((key) => key !== 'TOTAL')

  return (
    <TableContainer component={Paper} elevation={0} sx={{ border: '1px solid rgba(15, 23, 42, 0.08)' }}>
      {title ? (
        <Typography variant="h6" sx={{ px: 2, pt: 2, fontWeight: 700 }}>
          {title}
        </Typography>
      ) : null}
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Categoria</TableCell>
            {meses.map((mes) => (
              <TableCell key={mes} align="right">
                {mes}
              </TableCell>
            ))}
            <TableCell align="right">Total</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {categorias.map((categoria) => (
            <TableRow key={categoria} hover>
              <TableCell sx={{ fontWeight: 600 }}>{categoria}</TableCell>
              {meses.map((mes) => (
                <TableCell key={mes} align="right">
                  {currencyFormatter.format(reporte[categoria][mes] || 0)}
                </TableCell>
              ))}
              <TableCell align="right" sx={{ fontWeight: 700 }}>
                {currencyFormatter.format(reporte[categoria].TOTAL || 0)}
              </TableCell>
            </TableRow>
          ))}
          <TableRow>
            <TableCell sx={{ fontWeight: 800 }}>Total general</TableCell>
            {meses.map((mes) => (
              <TableCell key={mes} align="right" sx={{ fontWeight: 800 }}>
                {currencyFormatter.format(reporte.TOTAL?.[mes] || 0)}
              </TableCell>
            ))}
            <TableCell align="right" sx={{ fontWeight: 800 }}>
              {currencyFormatter.format(reporte.TOTAL?.TOTAL || 0)}
            </TableCell>
          </TableRow>
        </TableBody>
      </Table>
    </TableContainer>
  )
}

export default ReportTable
