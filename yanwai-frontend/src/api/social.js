import request from './index'

// ==================== 签到系统 ====================

/**
 * 每日签到
 */
export function checkin() {
  return request({
    url: '/api/social/checkin',
    method: 'post'
  })
}

/**
 * 获取签到状态
 */
export function getCheckinStatus() {
  return request({
    url: '/api/social/checkin/status',
    method: 'get'
  })
}

/**
 * 获取签到排行榜
 */
export function getCheckinRanking(limit = 20) {
  return request({
    url: '/api/social/checkin/ranking',
    method: 'get',
    params: { limit }
  })
}

// ==================== 好友系统 ====================

/**
 * 发送好友申请
 */
export function sendFriendRequest(toUserId, message = '') {
  return request({
    url: '/api/social/friend/request',
    method: 'post',
    data: { toUserId, message }
  })
}

/**
 * 处理好友申请
 */
export function handleFriendRequest(requestId, accept) {
  return request({
    url: `/api/social/friend/request/${requestId}/handle`,
    method: 'post',
    params: { accept }
  })
}

/**
 * 获取待处理的好友申请
 */
export function getPendingRequests() {
  return request({
    url: '/api/social/friend/requests',
    method: 'get'
  })
}

/**
 * 获取好友列表
 */
export function getFriendList() {
  return request({
    url: '/api/social/friend/list',
    method: 'get'
  })
}

/**
 * 删除好友
 */
export function removeFriend(friendId) {
  return request({
    url: `/api/social/friend/${friendId}`,
    method: 'delete'
  })
}

// ==================== 排行榜 ====================

/**
 * 获取卡牌排行榜
 */
export function getCardRanking(limit = 20) {
  return request({
    url: '/api/social/ranking/cards',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取分析次数排行榜
 */
export function getAnalysisRanking(limit = 20) {
  return request({
    url: '/api/social/ranking/analysis',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取成就排行榜
 */
export function getAchievementRanking(limit = 20) {
  return request({
    url: '/api/social/ranking/achievements',
    method: 'get',
    params: { limit }
  })
}

// ==================== 回复建议 ====================

/**
 * 生成回复建议
 */
export function generateReplySuggestion(analysisId) {
  return request({
    url: `/api/social/reply-suggestion/${analysisId}`,
    method: 'post'
  })
}

/**
 * 获取回复建议
 */
export function getReplySuggestion(analysisId) {
  return request({
    url: `/api/social/reply-suggestion/${analysisId}`,
    method: 'get'
  })
}
