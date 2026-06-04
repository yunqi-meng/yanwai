import request from '@/utils/request'

export function login(data) {
  return request.post('/login', data)
}

export function getAdminInfo() {
  return request.get('/info')
}
