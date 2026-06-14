import request from '../utils/request'

export function chatWithAI(data) {
  return request({
    url: '/ai/chat',
    method: 'post',
    data
  })
}

export function getSessions() {
  return request({
    url: '/ai/sessions',
    method: 'get'
  })
}

export function deleteSession(sessionId) {
  return request({
    url: `/ai/session/${sessionId}`,
    method: 'delete'
  })
}

export function getSessionMessages(sessionId) {
  return request({
    url: `/ai/session/${sessionId}/messages`,
    method: 'get'
  })
}
