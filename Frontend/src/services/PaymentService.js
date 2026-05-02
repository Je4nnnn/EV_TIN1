import { apiClient, getApiErrorMessage } from './api'

const API_URL = '/api/v1/payments'

export const simulatePayment = async ({ reservationId, amount, cardNumber, expirationDate, cvv }) => {
  try {
    const response = await apiClient.post(`${API_URL}/simulate`, {
      reservationId,
      amount,
      paymentMethod: 'CREDIT_CARD_SIMULATED',
      cardNumber,
      expirationDate,
      cvv,
    })
    return response.data
  } catch (error) {
    throw new Error(getApiErrorMessage(error, 'No fue posible registrar el pago simulado.'))
  }
}
