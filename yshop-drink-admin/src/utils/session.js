const TOKEN_KEY = 'owner_token'
const USER_KEY = 'owner_user'

function getStorage() {
  if (typeof window === 'undefined') {
    return null
  }
  return window.localStorage
}

export function getToken() {
  const storage = getStorage()
  return storage?.getItem(TOKEN_KEY) || ''
}

export function getUser() {
  const storage = getStorage()
  const raw = storage?.getItem(USER_KEY)
  if (!raw) {
    return null
  }

  try {
    return JSON.parse(raw)
  } catch {
    storage?.removeItem(USER_KEY)
    return null
  }
}

export function saveSession(token, user) {
  const storage = getStorage()
  if (!storage) {
    return
  }

  storage.setItem(TOKEN_KEY, token)
  storage.setItem(USER_KEY, JSON.stringify(user))
}

export function clearSession() {
  const storage = getStorage()
  storage?.removeItem(TOKEN_KEY)
  storage?.removeItem(USER_KEY)
}
