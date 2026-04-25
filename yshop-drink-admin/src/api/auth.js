import http from './http.js'

export function loginByPassword(payload) {
  return http.post('/api/auth/staff/login', payload)
}
