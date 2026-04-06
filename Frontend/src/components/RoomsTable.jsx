import React, { useState, useEffect } from 'react';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import { getRooms } from '../services/RoomsService';

export default function RoomsTable() {
  const [rooms, setRooms] = useState([]);

  useEffect(() => {
    const fetchRooms = async () => {
      try {
        const response = await getRooms();
        setRooms(Array.isArray(response) ? response : response.data || []);
      } catch (error) {
        console.error('Error fetching rooms:', error);
        setRooms([]); 
      }
    };

    fetchRooms();
  }, []);

  return (
    <TableContainer component={Paper}>
      <Table sx={{ minWidth: 650 }} aria-label="rooms table">
        <TableHead>
          <TableRow>
            <TableCell>Room ID</TableCell>
            <TableCell align="right">Room Number</TableCell>
            <TableCell align="right">Type</TableCell>
            <TableCell align="right">Status</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {Array.isArray(rooms) && rooms.length > 0 ? (
            rooms.map((room) => (
              <TableRow
                key={room.id}
                sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
              >
                <TableCell component="th" scope="row">
                  {room.id}
                </TableCell>
                <TableCell align="right">{room.roomNumber}</TableCell>
                <TableCell align="right">{room.type}</TableCell>
                <TableCell align="right">{room.status}</TableCell>
              </TableRow>
            ))
          ) : (
            <TableRow>
              <TableCell colSpan={4} align="center">
                No hay habitaciones
              </TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>
    </TableContainer>
  );
}