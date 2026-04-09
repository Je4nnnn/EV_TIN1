import { apiClient, getApiErrorMessage } from './api'

const API_URL = '/api/v1/tourist-packages'

export const getTouristPackages = async (availableOnly = false) => {
  try {
    const response = await apiClient.get(API_URL, {
      params: { availableOnly },
    })
    return Array.isArray(response.data) ? response.data : []
  } catch (error) {
    throw new Error(getApiErrorMessage(error, 'No fue posible obtener los paquetes turisticos.'))
  }
}

export const createTouristPackage = async (payload) => {
  try {
    const response = await apiClient.post(API_URL, payload)
    return response.data
  } catch (error) {
    throw new Error(getApiErrorMessage(error, 'No fue posible crear el paquete turistico.'))
  }
}

export const updateTouristPackage = async (id, payload) => {
  try {
    const response = await apiClient.put(`${API_URL}/${id}`, payload)
    return response.data
  } catch (error) {
    throw new Error(getApiErrorMessage(error, 'No fue posible actualizar el paquete turistico.'))
  }
}

export const updateTouristPackageAvailability = async (id, available) => {
  try {
    const response = await apiClient.patch(`${API_URL}/${id}/availability`, null, {
      params: { available },
    })
    return response.data
  } catch (error) {
    throw new Error(getApiErrorMessage(error, 'No fue posible actualizar la disponibilidad del paquete.'))
  }
}

export const deleteTouristPackage = async (id) => {
  try {
    const response = await apiClient.delete(`${API_URL}/${id}`)
    return response.data
  } catch (error) {
    throw new Error(getApiErrorMessage(error, 'No fue posible eliminar el paquete turistico.'))
  }
}
