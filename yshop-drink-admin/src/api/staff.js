import http from './http.js'

export function fetchStaffList() {
  return http.get('/api/owner/staff/list')
}

export function createStaff(payload) {
  return http.post('/api/owner/staff/add', payload)
}

export function updateStaff(payload) {
  return http.put('/api/owner/staff/update', payload)
}

export function deleteStaff(id) {
  return http.delete(`/api/owner/staff/${id}`)
}
