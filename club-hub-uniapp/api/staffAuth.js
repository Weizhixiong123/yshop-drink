import api from './api'

/**
 * 店员微信手机号授权登录
 * 前端通过 wx.getPhoneNumber 拿到 code，发给后端
 * 后端用 code 换取手机号 → 查员工表 → 返回 JWT
 *
 * @param {Object} data
 * @param {string} data.code - wx.getPhoneNumber 返回的 code
 * @returns {Promise} Result { code, msg, data: { token, role, ... } }
 */
export function staffWxLogin(data) {
  return api.post('/api/auth/staff/wx-login', data, { login: false })
}

/**
 * 店东用户名密码登录 (POST /api/auth/staff/login)
 *
 * @param {Object} data - StaffLoginRequest
 * @param {string} data.username - 用户名/手机号 (必填)
 * @param {string} data.password - 密码 (必填)
 * @returns {Promise} Result { code, msg, data: { token, ... } }
 */
export function staffLogin(data) {
  return api.post('/api/auth/staff/login', data, { login: false })
}
