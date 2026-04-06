import axios from 'axios';

const API_URL = `${import.meta.env.VITE_PAYROLL_BACKEND_SERVER}/api/v1/reservations`;

export const fetchReporteIngresos = async (fechaInicio, fechaFin) => {
    try {
        const response = await axios.get(`${API_URL}/reporte/tipo-habitacion`, {
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
        const response = await axios.get(`${API_URL}/reporte/cantidad-personas`, {
            params: { fechaInicio, fechaFin },
        });
        return response.data;
    } catch (error) {
        console.error('Error fetching report by participants:', error);
        throw error;
    }
};