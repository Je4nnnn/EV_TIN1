import axios from 'axios';

const API_URL = `${import.meta.env.VITE_PAYROLL_BACKEND_SERVER}/api/reserves/`;
const USER_API_URL = `${import.meta.env.VITE_PAYROLL_BACKEND_SERVER}/api/users/`;

export const processParticipants = async (personas) => {
  try {
    const participantesConUsuarios = await Promise.all(
      personas.map(async (persona) => {
        if (!persona.nombre || !persona.rut) {
          throw new Error('Debe completar los datos de todos los participantes (nombre y RUT).');
        }

        let usuario = await getUserByRut(persona.rut);

        if (!usuario) { //si no encuentra al usuario que lo cree
          usuario = await createUser({
            nombre: persona.nombre,
            rut: persona.rut,
            email: persona.email || '',
            telefono: persona.telefono || '',
          });
        } else {
          await incrementVisitsAndUpdateCategory(usuario.id);
        }

        return {
          ...persona,
          userId: usuario.id,
        };
      })
    );

    return participantesConUsuarios;
  } catch (error) {
    console.error('Error al procesar los participantes:', error);
    throw error;
  }
};

export const getReserves = async () => {
  try {
    const response = await axios.get(API_URL);
    return response.data;
  } catch (error) {
    console.error('Error fetching reserves:', error);
    throw error;
  }
};

export const createReserve = async (reserve) => {
  try {
    const response = await axios.post(API_URL, reserve, {
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
    const response = await axios.post(`${API_URL}confirmar`, reserve, {
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
    const response = await axios.get(`${USER_API_URL}findByRut/${rut}`);
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
    const response = await axios.post(USER_API_URL, user, {
      headers: { 'Content-Type': 'application/json' },
    });
    return response.data;
  } catch (error) {
    console.error('Error creating user:', error);
    throw error;
  }
};

export const incrementVisitsAndUpdateCategory = async (userId) => {
  try {
    const response = await axios.put(`${USER_API_URL}${userId}/increment-visits`);
    return response.data; 
  } catch (error) {
    console.error('Error incrementando visitas y actualizando categoría:', error);
    throw error; 
  }
};