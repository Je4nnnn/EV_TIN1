import axios from 'axios';

const API_URL = `${import.meta.env.VITE_PAYROLL_BACKEND_SERVER}/api/reserves/`;
export const getReservas = async () => {
  try {
    const response = await fetch(API_URL); 
    const data = await response.json();
    return Array.isArray(data) ? data : []; 
  } catch (error) {
    console.error('Error al obtener las reservas:', error);
    return [];
  }
};