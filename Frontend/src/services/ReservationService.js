import axios from 'axios';

const API_URL = `${import.meta.env.VITE_PAYROLL_BACKEND_SERVER}/api/v1/reservations`;
const USER_API_URL = `${import.meta.env.VITE_PAYROLL_BACKEND_SERVER}/api/users`;

const cleanRut = (rut) => {
  return rut ? rut.replace(/[.-]/g, '').toUpperCase() : '';
};

const formatDateToISO = (dateStr) => {
  if (!dateStr) return null;

  if (/^\d{4}-\d{2}-\d{2}$/.test(dateStr)) {
    return dateStr;
  }

  try {
    const d = new Date(dateStr);
    if (!isNaN(d.getTime())) {
      return d.toISOString().split('T')[0];
    }
  } catch (e) {
    console.warn('No se pudo formatear la fecha:', dateStr);
  }

  return null;
};

/**
 * Procesa los huéspedes: busca o crea usuarios por RUT.
 * OJO: aquí ya NO se incrementan visitas.
 * Las visitas deben incrementarse solo cuando la reserva se confirma en backend.
 */
export const processParticipants = async (personas) => {
  try {
    const participantesConUsuarios = await Promise.all(
        personas.map(async (persona) => {
          if (!persona.nombre || !persona.rut) {
            throw new Error('Debe completar los datos de todos los huéspedes (nombre y RUT).');
          }

          const cleanedRut = cleanRut(persona.rut);
          let usuario = await getUserByRut(cleanedRut);

          if (!usuario) {
            usuario = await createUser({
              name: persona.nombre,
              rut: cleanedRut,
              email: persona.email || '',
              phoneNumber: persona.telefono || '',
              dateBirthday: formatDateToISO(persona.fechaCumpleanos),
            });
          } else {
            await updateUser(usuario.id, {
              name: persona.nombre,
              email: persona.email || usuario.email || '',
              phoneNumber: persona.telefono || usuario.phoneNumber || '',
              dateBirthday: formatDateToISO(persona.fechaCumpleanos) || usuario.dateBirthday || null,
            });
          }

          return {
            ...persona,
            userId: usuario.id,
          };
        })
    );

    return participantesConUsuarios;
  } catch (error) {
    console.error('Error al procesar los huéspedes:', error);
    throw error;
  }
};

export const getReserves = async () => {
  try {
    const response = await axios.get(`${API_URL}/`);
    return response.data;
  } catch (error) {
    console.error('Error fetching reserves:', error);
    throw error;
  }
};

export const createReserve = async (reserve) => {
  try {
    const response = await axios.post(`${API_URL}/`, reserve, {
      headers: { 'Content-Type': 'application/json' },
    });
    return response.data;
  } catch (error) {
    console.error('Error creating reserve:', error);
    throw error;
  }
};

export const confirmReserve = async (reserve) => {
  try {
    const response = await axios.post(`${API_URL}/confirmar`, reserve, {
      headers: { 'Content-Type': 'application/json' },
    });
    return response.data;
  } catch (error) {
    console.error('Error confirming reserve:', error);
    throw error;
  }
};

export const getUserByRut = async (rut) => {
  try {
    const cleanedRut = cleanRut(rut);
    const response = await axios.get(`${USER_API_URL}/findByRut/${encodeURIComponent(cleanedRut)}`);
    return response.data;
  } catch (error) {
    if (error.response && error.response.status === 404) {
      return null;
    }
    console.error('Error fetching user by RUT:', error);
    throw error;
  }
};

export const createUser = async (user) => {
  try {
    const response = await axios.post(`${USER_API_URL}/`, user, {
      headers: { 'Content-Type': 'application/json' },
    });
    return response.data;
  } catch (error) {
    console.error('Error creating user:', error);
    throw error;
  }
};

export const updateUser = async (userId, userdata) => {
  try {
    const response = await axios.put(`${USER_API_URL}/${userId}`, userdata, {
      headers: { 'Content-Type': 'application/json' },
    });
    return response.data;
  } catch (error) {
    console.error('Error updating user:', error);
    // No lanzar error si falla actualización de datos no críticos
    return null;
  }
};

export const incrementVisitsAndUpdateCategory = async (userId) => {
  try {
    const response = await axios.put(`${USER_API_URL}/${userId}/increment-visits`);
    return response.data;
  } catch (error) {
    console.error('Error incrementando visitas y actualizando categoría:', error);
    throw error;
  }
};