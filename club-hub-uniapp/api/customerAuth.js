import api from './api'

export function customerLogin(data) {
  return api.post('/api/auth/customer/login', data, { login: false })
}

export function customerWxLogin(data) {
  return api.post('/api/auth/customer/wx-login', data, { login: false })
}
