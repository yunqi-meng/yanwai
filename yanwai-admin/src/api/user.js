import request from '@/utils/request'

export function getUserList(params) {
  return request.get('/users', { params })
}

export function getUserDetail(id) {
  return request.get(`/users/${id}`)
}

export function updateUser(id, data) {
  return request.put(`/users/${id}`, data)
}

export function banUser(id) {
  return request.post(`/users/${id}/ban`)
}

export function unbanUser(id) {
  return request.post(`/users/${id}/unban`)
}

export function updateMemberLevel(id, data) {
  return request.post(`/users/${id}/member`, data)
}
