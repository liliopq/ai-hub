import request from '../utils/request'

export function getUserInfo() {
  return request({
    url: '/user/me',
    method: 'get'
  })
}

export function updateUserInfo(data) {
  return request({
    url: '/user/me',
    method: 'put',
    data
  })
}

export function changePassword(data) {
  return request({
    url: '/user/me/password',
    method: 'put',
    data
  })
}

export function getUserById(userId) {
  return request({
    url: `/user/${userId}`,
    method: 'get'
  })
}
