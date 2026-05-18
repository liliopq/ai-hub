import SockJS from 'sockjs-client'
import Stomp from 'stompjs'
import { getToken } from './auth'

let stompClient = null
let reconnectAttempts = 0
const MAX_RECONNECT_ATTEMPTS = 5
const RECONNECT_DELAY = 3000

export function connect(callback) {
  if (stompClient?.connected) {
    console.log('WebSocket 已连接')
    return
  }

  const token = getToken()
  if (!token) {
    console.warn('未登录，跳过 WebSocket 连接')
    return
  }

  const socket = new SockJS(`${import.meta.env.VITE_API_BASE_URL}/ws/notification`)
  
  stompClient = Stomp.over(socket)
  stompClient.debug = () => {}

  stompClient.connect(
    { token: `Bearer ${token}` },
    (frame) => {
      console.log('WebSocket 连接成功:', frame)
      reconnectAttempts = 0

      // 订阅用户专属频道
      const userId = getUserIdFromToken()
      if (userId) {
        const destination = `/user/${userId}/notification`
        stompClient.subscribe(destination, (message) => {
          try {
            const notification = JSON.parse(message.body)
            console.log('收到通知:', notification)
            if (callback) {
              callback(notification)
            }
          } catch (e) {
            console.error('解析通知消息失败:', e)
          }
        })
      }
    },
    (error) => {
      console.error('WebSocket 连接失败:', error)
      reconnectAttempts++
      if (reconnectAttempts <= MAX_RECONNECT_ATTEMPTS) {
        console.log(`第 ${reconnectAttempts} 次尝试重新连接...`)
        setTimeout(() => connect(callback), RECONNECT_DELAY * reconnectAttempts)
      } else {
        console.error('已达到最大重连次数，停止尝试')
      }
    }
  )
}

export function disconnect() {
  if (stompClient) {
    stompClient.disconnect(() => {
      console.log('WebSocket 连接已断开')
    })
    stompClient = null
  }
}

export function isConnected() {
  return stompClient?.connected ?? false
}

function getUserIdFromToken() {
  try {
    const token = getToken()
    if (!token) return null
    const payload = token.split('.')[1]
    const decoded = atob(payload)
    const data = JSON.parse(decoded)
    return data.userId
  } catch (e) {
    console.error('解析 Token 失败:', e)
    return null
  }
}
