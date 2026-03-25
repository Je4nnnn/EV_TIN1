import * as React from 'react';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';

function createData(vueltas, precios, duracion) {
  return {vueltas, precios, duracion};
}

const rows = [
  createData('10 vueltas o máx 10 min', '$15.000', '30 min'),
  createData('15 vueltas o máx 15 min', '$20.000', '35 min'),
  createData('20 vueltas o máx 20 min', '$25.000', '40 min'),
];

// Tablita para ver los precios por si alguien queire consultar

export default function BasicTablePrices() {
  return (
    <TableContainer component={Paper}>
      <Table sx={{ minWidth: 650 }} aria-label="simple table">
        <TableHead>
          <TableRow>
            <TableCell>Número de vueltas o tiempo máximo permitido</TableCell>
            <TableCell align="right">Precios regulares</TableCell>
            <TableCell align="right">Duración total de la reserva</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {rows.map((row) => (
            <TableRow
              key={row.name}
              sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
            >
              <TableCell component="th" scope="row">
                {row.vueltas}
              </TableCell>
              <TableCell align="right">{row.precios}</TableCell>
              <TableCell align="right">{row.duracion}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
}
