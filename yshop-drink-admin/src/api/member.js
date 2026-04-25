import http from './http.js'

export function fetchMemberList(params) {
  return http.get('/api/owner/member/list', { params })
}

export function updateMemberInfo(payload) {
  return http.put('/api/owner/member/info', payload)
}

export function updateMemberRemark(payload) {
  return http.put('/api/owner/member/remark', payload)
}

export function operateMember(payload) {
  return http.post('/api/owner/member/operate', payload)
}
