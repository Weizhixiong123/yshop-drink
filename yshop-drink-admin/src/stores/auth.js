import { reactive } from 'vue'

export const authStore = reactive({
  isLoggedIn: false,
  user: null,

  login(username, password) {
    if (username === 'boss' && password === 'boss') {
      this.isLoggedIn = true
      this.user = { name: '店东 Boss', role: 'boss' }
      return true
    }
    return false
  },

  logout() {
    this.isLoggedIn = false
    this.user = null
  }
})
