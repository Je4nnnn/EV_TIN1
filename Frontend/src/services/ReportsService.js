import { apiClient, getApiErrorMessage } from './api'

const API_URL = '/api/v1/reservations'

export const fetchReporteIngresos = async (fechaInicio, fechaFin) => {
  try {
    const response = await apiClient.get(`${API_URL}/reports/room-type`, {
      params: { fechaInicio, fechaFin },
    })
    return response.data
  } catch (error) {
    throw new Error(getApiErrorMessage(error, 'No fue posible obtener el reporte de ingresos.'))
  }
}

export const fetchReporteParticipantes = async (fechaInicio, fechaFin) => {
  try {
    const response = await apiClient.get(`${API_URL}/reports/guest-count`, {
      params: { fechaInicio, fechaFin },
    })
    return response.data
  } catch (error) {
    throw new Error(getApiErrorMessage(error, 'No fue posible obtener el reporte por participantes.'))
  }
}
