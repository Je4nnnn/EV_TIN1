import axios from 'axios';

const API_URL = `${import.meta.env.VITE_PAYROLL_BACKEND_SERVER}/api/karts/`;

export const getKarts = async () => {
  try {
    const response = await axios.get(API_URL);
    return response.data;
  } catch (error) {
    console.error('Error fetching karts:', error);
    throw error; 
  }
};