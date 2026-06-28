import request from '../utils/request'

export function getUserList(params) {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  })
}

export function updateUserStatus(userId, data) {
  return request({
    url: `/admin/users/${userId}/status`,
    method: 'put',
    data
  })
}

export function getPostList(params) {
  return request({
    url: '/admin/posts',
    method: 'get',
    params
  })
}

export function auditPost(postId, data) {
  return request({
    url: `/admin/posts/${postId}/audit`,
    method: 'put',
    data
  })
}

export function setPostSticky(postId, data) {
  return request({
    url: `/admin/posts/${postId}/sticky`,
    method: 'put',
    data
  })
}

export function deleteComment(commentId) {
  return request({
    url: `/admin/comments/${commentId}`,
    method: 'delete'
  })
}
