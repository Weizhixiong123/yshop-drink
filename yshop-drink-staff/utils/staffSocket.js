import { VUE_APP_WS_URL } from '@/config'
import cookie from '@/utils/cookie'

let socketTask = null
let reconnectTimer = null
let reconnectAttempts = 0
let manuallyClosed = false
let socketStatus = 'closed'

export function connectStaffSocket() {
  const token = cookie.get('accessToken')
  if (!token || socketStatus === 'connecting' || socketStatus === 'open') {
    return
  }

  manuallyClosed = false
  socketStatus = 'connecting'
  const url = VUE_APP_WS_URL + '?token=' + encodeURIComponent(token)
  socketTask = uni.connectSocket({
    url,
    header: {
      Authorization: 'Bearer ' + token,
    },
    complete() {},
  })

  socketTask.onOpen(() => {
    socketStatus = 'open'
    reconnectAttempts = 0
  })

  socketTask.onMessage((event) => {
    const message = parseMessage(event.data)
    if (message && message.type) {
      uni.$emit(message.type, message)
    }
  })

  socketTask.onClose(() => {
    socketTask = null
    socketStatus = 'closed'
    scheduleReconnect()
  })

  socketTask.onError(() => {
    socketTask = null
    socketStatus = 'closed'
    scheduleReconnect()
  })
}

export function disconnectStaffSocket() {
  manuallyClosed = true
  clearReconnectTimer()
  if (socketTask) {
    socketStatus = 'closing'
    socketTask.close()
    return
  }
  socketStatus = 'closed'
}

function scheduleReconnect() {
  if (manuallyClosed || reconnectTimer || !cookie.get('accessToken')) {
    return
  }

  const delay = Math.min(30000, 1000 * Math.pow(2, reconnectAttempts))
  reconnectAttempts += 1
  reconnectTimer = setTimeout(() => {
    reconnectTimer = null
    connectStaffSocket()
  }, delay)
}

function clearReconnectTimer() {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
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
