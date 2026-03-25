import axios from 'axios';

const API_URL = `${import.meta.env.VITE_PAYROLL_BACKEND_SERVER}/api/reserves/`;

export const fetchReporteIngresos = async (fechaInicio, fechaFin) => {
    try {
        const response = await axios.get(`${API_URL}ingresos-por-vueltas-tiempo`, {
            params: { fechaInicio, fechaFin },
        });
        return response.data;
    } catch (error) {
        console.error('Error fetching report:', error);
        throw error;
    }
};

export const fetchReporteParticipantes = async (fechaInicio, fechaFin) => {
  try {
      const response = await axios.get(`${API_URL}ingresos-por-cantidad-personas`, {
          params: { fechaInicio, fechaFin },
      });
      return response.data;
  } catch (error) {
      console.error('Error fetching report by participants:', error);
      throw error;
  }
};