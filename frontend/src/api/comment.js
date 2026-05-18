import request from '../utils/request'

export function createComment(data) {
  return request({
    url: '/comment',
    method: 'post',
    data
  })
}

export function getCommentList(postId, params) {
  return request({
    url: `/comment/list/${postId}`,
    method: 'get',
    params
  })
}

export function deleteComment(commentId) {
  return request({
    url: `/comment/${commentId}`,
    method: 'delete'
  })
}

export function likeComment(commentId) {
  return request({
    url: `/comment/${commentId}/like`,
    method: 'post'
  })
}
