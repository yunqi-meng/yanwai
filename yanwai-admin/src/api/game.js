import request from '@/utils/request'

// 卡牌管理
export function getCardList(params) {
  return request.get('/cards', { params })
}

export function getCardDetail(id) {
  return request.get(`/cards/${id}`)
}

export function createCard(data) {
  return request.post('/cards', data)
}

export function updateCard(id, data) {
  return request.put(`/cards/${id}`, data)
}

export function deleteCard(id) {
  return request.delete(`/cards/${id}`)
}

// 成就管理
export function getAchievementList(params) {
  return request.get('/achievements', { params })
}

export function getAchievementDetail(id) {
  return request.get(`/achievements/${id}`)
}

export function createAchievement(data) {
  return request.post('/achievements', data)
}

export function updateAchievement(id, data) {
  return request.put(`/achievements/${id}`, data)
}

export function deleteAchievement(id) {
  return request.delete(`/achievements/${id}`)
}
