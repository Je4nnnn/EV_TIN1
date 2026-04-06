import axios from 'axios';

const API_URL = `${import.meta.env.VITE_PAYROLL_BACKEND_SERVER}/api/v1/reservations/`;

export const getReservas = async () => {
  try {
    const response = await axios.get(API_URL);
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error('Error al obtener las reservas:', error);
    return [];
  }
};