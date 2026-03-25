import React, { useState, useEffect } from 'react';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import { getKarts } from '../services/KartsService';

export default function KartsTable() {
  const [karts, setKarts] = useState([]);

  useEffect(() => {
    const fetchKarts = async () => {
      try {
        const response = await getKarts();
        setKarts(Array.isArray(response) ? response : response.data || []);
      } catch (error) {
        console.error('Error fetching karts:', error);
        setKarts([]); 
      }
    };

    fetchKarts();
  }, []);

  return (
    <TableContainer component={Paper}>
      <Table sx={{ minWidth: 650 }} aria-label="karts table">
        <TableHead>
          <TableRow>
            <TableCell>Kart ID</TableCell>
            <TableCell align="right">Code</TableCell>
            <TableCell align="right">Model</TableCell>
            <TableCell align="right">Status</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {Array.isArray(karts) && karts.length > 0 ? (
            karts.map((kart) => (
              <TableRow
                key={kart.id}
                sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
              >
                <TableCell component="th" scope="row">
                  {kart.id}
                </TableCell>
                <TableCell align="right">{kart.code}</TableCell>
                <TableCell align="right">{kart.model}</TableCell>
                <TableCell align="right">{kart.status}</TableCell>
              </TableRow>
            ))
          ) : (
            <TableRow>
              <TableCell colSpan={4} align="center">
                No hay carritos
              </TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>
    </TableContainer>
  );
}