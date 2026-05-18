import request from '../utils/request'

export function followUser(followeeId) {
  return request({
    url: `/follow/${followeeId}`,
    method: 'post'
  })
}

export function unfollowUser(followeeId) {
  return request({
    url: `/follow/${followeeId}`,
    method: 'delete'
  })
}

export function checkFollowing(followeeId) {
  return request({
    url: `/follow/check/${followeeId}`,
    method: 'get'
  })
}

export function getFollowCounts(userId) {
  return request({
    url: `/follow/count/${userId}`,
    method: 'get'
  })
}

export function getFollowingList(userId) {
  return request({
    url: `/follow/following/${userId}`,
    method: 'get'
  })
}

export function getFollowerList(userId) {
  return request({
    url: `/follow/followers/${userId}`,
    method: 'get'
  })
}
