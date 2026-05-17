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

export function updateMemberLevel(payload) {
  return http.put('/api/owner/member/level', payload)
}

export function fetchAccountStat(params) {
  return http.get('/api/owner/account/stat', { params })
}

export async function exportAccount(date) {
  const response = await http.get('/api/owner/account/export', {
    params: date ? { date } : {},
    responseType: 'blob'
  })
  const blob = new Blob([response.data], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `门店对账-${date || '今日'}.xlsx`
  link.click()
  window.URL.revokeObjectURL(url)
}
