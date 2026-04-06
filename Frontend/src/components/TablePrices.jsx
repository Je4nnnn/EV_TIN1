import * as React from 'react';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';

const rows = [
  {
    tipo:     'Simple',
    manana:   '$30.000',
    noche:    '$50.000',
    completo: '$70.000',
  },
  {
    tipo:     'Doble',
    manana:   '$48.000',
    noche:    '$80.000',
    completo: '$112.000',
  },
  {
    tipo:     'Suite',
    manana:   '$90.000',
    noche:    '$150.000',
    completo: '$210.000',
  },
];

export default function BasicTablePrices() {
  return (
    <>
      <Typography variant="h6" gutterBottom>
        Tarifas por Habitación y Turno
      </Typography>
      <TableContainer component={Paper}>
        <Table sx={{ minWidth: 400 }} aria-label="tabla de precios">
          <TableHead>
            <TableRow>
              <TableCell><strong>Habitación</strong></TableCell>
              <TableCell align="right"><strong>Turno Mañana</strong></TableCell>
              <TableCell align="right"><strong>Turno Noche</strong></TableCell>
              <TableCell align="right"><strong>Día Completo</strong></TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.map((row) => (
              <TableRow
                key={row.tipo}
                sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
              >
                <TableCell component="th" scope="row">{row.tipo}</TableCell>
                <TableCell align="right">{row.manana}</TableCell>
                <TableCell align="right">{row.noche}</TableCell>
                <TableCell align="right">{row.completo}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
      <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
        * Precios por noche. Descuentos por frecuencia y grupo se aplican automáticamente.
      </Typography>
    </>
  );
}
