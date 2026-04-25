import { reactive } from 'vue'
import { loginByPassword } from '../api/auth.js'
import { clearSession, getToken, getUser, saveSession } from '../utils/session.js'

const initialToken = getToken()
const initialUser = getUser()

function resolveApiMessage(error, fallback) {
  return error?.response?.data?.msg || error?.message || fallback
}

export const authStore = reactive({
  token: initialToken,
  isLoggedIn: Boolean(initialToken),
  user: initialUser,

  async login(username, password) {
    try {
      const response = await loginByPassword({
        username: username.trim(),
        password
      })
      const result = response.data

      if (result?.code !== 200 || !result?.data?.token) {
        throw new Error(result?.msg || '登录失败')
      }

      const user = {
        name: result.data.name || '店东',
        role: result.data.role || 'owner'
      }

      this.token = result.data.token
      this.isLoggedIn = true
      this.user = user
      saveSession(this.token, user)
      return true
    } catch (error) {
      throw new Error(resolveApiMessage(error, '登录失败'))
    }
  },

  logout() {
    this.token = ''
    this.isLoggedIn = false
    this.user = null
    clearSession()
  }
})
