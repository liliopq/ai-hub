export function getToken() {
  return localStorage.getItem('token')
}

export function setToken(token) {
  localStorage.setItem('token', token)
}

export function removeToken() {
  localStorage.removeItem('token')
}

export function getUser() {
  const user = localStorage.getItem('user')
  return user ? JSON.parse(user) : null
}

export function setUser(user) {
  localStorage.setItem('user', JSON.stringify(user))
  // 触发自定义事件，通知其他组件用户信息已更新
  window.dispatchEvent(new CustomEvent('user-updated', { detail: user }))
}

export function removeUser() {
  localStorage.removeItem('user')
}

export function isLoggedIn() {
  return !!getToken()
}

export function isAdmin() {
  const user = getUser()
  return user && user.role === 'ADMIN'
}

export function getUserId() {
  const user = getUser()
  return user ? user.id : null
}
