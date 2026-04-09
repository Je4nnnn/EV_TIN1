import { apiClient, getApiErrorMessage } from './api'

const RESERVATIONS_API_URL = '/api/v1/reservations'
const USERS_API_URL = '/api/v1/users'

const cleanRut = (rut) => (rut ? rut.replace(/[.-]/g, '').trim().toUpperCase() : '')

const formatDateToISO = (dateStr) => {
  if (!dateStr) return null
  if (/^\d{4}-\d{2}-\d{2}$/.test(dateStr)) return dateStr

  const parsedDate = new Date(dateStr)
  if (Number.isNaN(parsedDate.getTime())) {
    return null
  }

  return parsedDate.toISOString().split('T')[0]
}

export const processParticipants = async (personas) => {
  try {
    const participants = await Promise.all(
      personas.map(async (persona) => {
        if (!persona.nombre?.trim() || !persona.rut?.trim()) {
          throw new Error('Debe completar nombre y RUT de todos los huespedes.')
        }

        const cleanedRut = cleanRut(persona.rut)
        let user = await getUserByRut(cleanedRut)

        if (!user) {
          user = await createUser({
            name: persona.nombre.trim(),
            rut: cleanedRut,
            email: persona.email?.trim() || '',
            phoneNumber: persona.telefono?.trim() || '',
            dateBirthday: formatDateToISO(persona.fechaCumpleanos),
          })
        } else {
          const updatedUser = await updateUser(user.id, {
            name: persona.nombre.trim(),
            email: persona.email?.trim() || user.email || '',
            phoneNumber: persona.telefono?.trim() || user.phoneNumber || '',
            dateBirthday: formatDateToISO(persona.fechaCumpleanos) || user.dateBirthday || null,
          })

          user = updatedUser || user
        }

        return {
          ...persona,
          userId: user.id,
        }
      }),
    )

    return participants
  } catch (error) {
    throw new Error(getApiErrorMessage(error, 'No fue posible procesar los huespedes.'))
  }
}

export const getReservations = async () => {
  try {
    const response = await apiClient.get(RESERVATIONS_API_URL)
    return Array.isArray(response.data) ? response.data : []
  } catch (error) {
    throw new Error(getApiErrorMessage(error, 'No fue posible obtener las reservas.'))
  }
}

export const getReserves = getReservations

export const confirmReserve = async (reserve) => {
  try {
    const response = await apiClient.post(`${RESERVATIONS_API_URL}/confirmar`, reserve)
    return response.data
  } catch (error) {
    throw new Error(getApiErrorMessage(error, 'No fue posible confirmar la reserva.'))
  }
}

export const deleteReservation = async (reservationId) => {
  try {
    const response = await apiClient.delete(`${RESERVATIONS_API_URL}/${reservationId}`)
    return response.data
  } catch (error) {
    throw new Error(getApiErrorMessage(error, 'No fue posible cancelar la reserva.'))
  }
}

export const getUserByRut = async (rut) => {
  try {
    const response = await apiClient.get(`${USERS_API_URL}/findByRut/${encodeURIComponent(cleanRut(rut))}`)
    return response.data
  } catch (error) {
    if (error?.response?.status === 404) {
      return null
    }

    throw new Error(getApiErrorMessage(error, 'No fue posible consultar el usuario.'))
  }
}

export const createUser = async (user) => {
  try {
    const response = await apiClient.post(USERS_API_URL, user)
    return response.data
  } catch (error) {
    throw new Error(getApiErrorMessage(error, 'No fue posible crear el usuario.'))
  }
}

export const updateUser = async (userId, userData) => {
  try {
    const response = await apiClient.put(`${USERS_API_URL}/${userId}`, userData)
    return response.data
  } catch (error) {
    return null
  }
}
