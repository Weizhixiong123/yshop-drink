import api from './api'



/**
 *  顾客微信登录（使用手机号）
 */
export function customerLogin(data) {
  return api.post('/api/auth/customer/login', data, { login: false })
}

