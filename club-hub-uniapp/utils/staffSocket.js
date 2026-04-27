import { VUE_APP_CUSTOMER_WS_URL, VUE_APP_STAFF_WS_URL } from '@/config'
import cookie from '@/utils/cookie'

const sockets = {
  staff: createSocketState(VUE_APP_STAFF_WS_URL),
  customer: createSocketState(VUE_APP_CUSTOMER_WS_URL),
}

function createSocketState(url) {
  return {
    url,
    task: null,
    reconnectTimer: null,
    reconnectAttempts: 0,
    manuallyClosed: false,
    status: 'closed',
    token: '',
  }
}

export function connectStaffSocket() {
  connectSocket('staff')
}

export function connectCustomerSocket() {
  connectSocket('customer')
}

export function disconnectStaffSocket() {
  disconnectSocket('staff')
}

export function disconnectCustomerSocket() {
  disconnectSocket('customer')
}

function connectSocket(type) {
  const state = sockets[type]
  const token = cookie.get('accessToken')
  if (!state || !token || state.status === 'connecting') {
    return
  }

  if (state.status === 'open' && state.token === token) {
    return
  }

  if (state.task) {
    state.manuallyClosed = true
    state.task.close()
    state.task = null
  }

  state.manuallyClosed = false
  state.status = 'connecting'
  state.token = token
  const url = state.url + '?token=' + encodeURIComponent(token)
  state.task = uni.connectSocket({
    url,
    header: {
      Authorization: 'Bearer ' + token,
    },
    complete() {},
  })
  const currentTask = state.task

  currentTask.onOpen(() => {
    if (state.task !== currentTask) return
    state.status = 'open'
    state.reconnectAttempts = 0
  })

  currentTask.onMessage((event) => {
    if (state.task !== currentTask) return
    const message = parseMessage(event.data)
    if (message && message.type) {
      uni.$emit(message.type, message)
    }
  })

  currentTask.onClose(() => {
    if (state.task !== currentTask) return
    state.task = null
    state.status = 'closed'
    scheduleReconnect(type)
  })

  currentTask.onError(() => {
    if (state.task !== currentTask) return
    state.task = null
    state.status = 'closed'
    scheduleReconnect(type)
  })
}

function disconnectSocket(type) {
  const state = sockets[type]
  if (!state) {
    return
  }
  state.manuallyClosed = true
  clearReconnectTimer(state)
  if (state.task) {
    state.status = 'closing'
    state.task.close()
    return
  }
  state.status = 'closed'
  state.token = ''
}

function scheduleReconnect(type) {
  const state = sockets[type]
  if (!state || state.manuallyClosed || state.reconnectTimer || !cookie.get('accessToken')) {
    return
  }

  const delay = Math.min(30000, 1000 * Math.pow(2, state.reconnectAttempts))
  state.reconnectAttempts += 1
  state.reconnectTimer = setTimeout(() => {
    state.reconnectTimer = null
    connectSocket(type)
  }, delay)
}

function clearReconnectTimer(state) {
  if (state.reconnectTimer) {
    clearTimeout(state.reconnectTimer)
    state.reconnectTimer = null
  }
}

function parseMessage(data) {
  if (!data) {
    return null
  }
  if (typeof data === 'object') {
    return data
  }
  try {
    return JSON.parse(data)
  } catch (e) {
    return null
  }
}
