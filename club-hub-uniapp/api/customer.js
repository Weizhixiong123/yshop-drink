import api from './api'

export function customerInfo(phone) {
  return api.get('/api/customer/info', { phone })
}
