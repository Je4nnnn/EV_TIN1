import { apiClient, getApiErrorMessage } from './api'

const API_URL = '/api/v1/rooms'

export const getRooms = async () => {
  try {
    const response = await apiClient.get(API_URL)
    return Array.isArray(response.data) ? response.data : []
  } catch (error) {
    throw new Error(getApiErrorMessage(error, 'No fue posible obtener las habitaciones.'))
  }
}

export const getRoomOverview = async (referenceDate) => {
  try {
    const response = await apiClient.get(`${API_URL}/overview`, {
      params: referenceDate ? { referenceDate } : {},
    })
    return Array.isArray(response.data) ? response.data : []
  } catch (error) {
    throw new Error(getApiErrorMessage(error, 'No fue posible obtener el estado operativo de habitaciones.'))
  }
}

export const getAvailableRooms = async ({ checkInDate, checkOutDate, roomType, stayType }) => {
  try {
    const response = await apiClient.get(`${API_URL}/available`, {
      params: { checkInDate, checkOutDate, roomType, stayType },
    })
    return Array.isArray(response.data) ? response.data : []
  } catch (error) {
    throw new Error(getApiErrorMessage(error, 'No fue posible consultar la disponibilidad de habitaciones.'))
  }
}
