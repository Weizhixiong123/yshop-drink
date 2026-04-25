import { VUE_APP_WS_URL } from '@/config'
import { userGetUserInfo } from '@/api/user'
import { useMainStore } from '@/store/store'
import cookie from '@/utils/cookie'

let socketTask = null
let reconnectTimer = null
let reconnectAttempts = 0
let manuallyClosed = false
let refreshing = false

export function connectCustomerSocket() {
  const token = cookie.get('accessToken')
  if (!token || socketTask) {
    return
  }

  manuallyClosed = false
  const url = VUE_APP_WS_URL + '?token=' + encodeURIComponent(token)
  socketTask = uni.connectSocket({
    url,
    header: {
      Authorization: 'Bearer ' + token,
    },
    complete() {},
  })

  socketTask.onOpen(() => {
    reconnectAttempts = 0
  })

  socketTask.onMessage((event) => {
    const message = parseMessage(event.data)
    if (message && message.type === 'USER_DATA_UPDATE') {
      refreshCustomerInfo()
    }
  })

  socketTask.onClose(() => {
    socketTask = null
    scheduleReconnect()
  })

  socketTask.onError(() => {
    socketTask = null
    scheduleReconnect()
  })
}

export function disconnectCustomerSocket() {
  manuallyClosed = true
  clearReconnectTimer()
  if (socketTask) {
    socketTask.close()
    socketTask = null
  }
}

export async function refreshCustomerInfo() {
  if (refreshing) {
    return
  }

  const main = useMainStore()
  const userInfo = main.member && main.member.phone
    ? main.member
    : uni.getStorageSync('userinfo') || {}
  const phone = userInfo.phone || userInfo.mobile
  if (!phone) {
    return
  }

  refreshing = true
  try {
    const data = await userGetUserInfo({ phone })
    if (data) {
      data.phone = phone
      main.SET_MEMBER(data)
      uni.setStorageSync('userinfo', data)
    }
  } finally {
    refreshing = false
  }
}

function scheduleReconnect() {
  if (manuallyClosed || reconnectTimer || !cookie.get('accessToken')) {
    return
  }

  const delay = Math.min(30000, 1000 * Math.pow(2, reconnectAttempts))
  reconnectAttempts += 1
  reconnectTimer = setTimeout(() => {
    reconnectTimer = null
    connectCustomerSocket()
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
