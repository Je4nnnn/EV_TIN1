import axios from 'axios'

const baseURL =
  import.meta.env.VITE_API_BASE_URL ||
  import.meta.env.VITE_PAYROLL_BACKEND_SERVER ||
  'http://localhost:8091'

export const apiClient = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
})

export const getApiErrorMessage = (error, fallbackMessage = 'Ocurrio un error inesperado.') => {
  const responseData = error?.response?.data

  if (typeof responseData === 'string' && responseData.trim()) {
    return responseData
  }

  if (responseData?.message) {
    return responseData.message
  }

  if (responseData?.error) {
    return responseData.error
  }

  if (error?.message) {
    return error.message
  }

  return fallbackMessage
}
