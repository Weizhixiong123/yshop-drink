import http from './http.js'

export function fetchOperationLogList(params) {
  return http.get('/api/owner/log/list', { params })
}
