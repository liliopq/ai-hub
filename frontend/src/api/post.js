import request from '../utils/request'

export function createPost(data) {
  return request({
    url: '/post',
    method: 'post',
    data
  })
}

export function getPostList(params) {
  return request({
    url: '/post/list',
    method: 'get',
    params
  })
}

export function getAllTags() {
  return request({
    url: '/post/tags',
    method: 'get'
  })
}

export function getPostDetail(postId) {
  return request({
    url: `/post/${postId}`,
    method: 'get'
  })
}

export function updatePost(postId, data) {
  return request({
    url: `/post/${postId}`,
    method: 'put',
    data
  })
}

export function deletePost(postId) {
  return request({
    url: `/post/${postId}`,
    method: 'delete'
  })
}

export function likePost(postId, action) {
  return request({
    url: `/post/${postId}/like`,
    method: 'post',
    data: { action }
  })
}

export function collectPost(postId, action) {
  return request({
    url: `/post/${postId}/collect`,
    method: 'post',
    data: { action }
  })
}

export function getUserPosts(userId, params) {
  return request({
    url: `/post/user/${userId}`,
    method: 'get',
    params
  })
}

export function getUserLikedPosts(userId, params) {
  return request({
    url: `/post/user/${userId}/liked`,
    method: 'get',
    params
  })
}

export function getUserCollectedPosts(userId, params) {
  return request({
    url: `/post/user/${userId}/collected`,
    method: 'get',
    params
  })
}
