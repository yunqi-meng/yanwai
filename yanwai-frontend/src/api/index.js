import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '',
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json'
  }
})

api.interceptors.request.use(
  config => {
    const userId = localStorage.getItem('userId')
    if (userId) {
      config.headers['X-User-Id'] = userId
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

api.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return response
  },
  error => {
    ElMessage.error(error.message || '网络请求失败')
    return Promise.reject(error)
  }
)

export default {
  post: (url, data) => api.post(url, data),
  get: (url, params) => api.get(url, { params }),
  put: (url, data) => api.put(url, data),
  delete: (url) => api.delete(url)
}

export const analysisApi = {
  decode: (text) => api.post('/api/analysis/decode', { text }),
  uploadImage: (formData) => api.post('/api/analysis/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }),
  share: () => api.post('/api/analysis/share'),
  getHistory: (page, pageSize) => api.get('/api/analysis/history', { params: { page, pageSize } }),
  deleteHistory: (id) => api.delete(`/api/analysis/history/${id}`),
  getHistoryDetail: (id) => api.get(`/api/analysis/history/${id}`)
}

export const cardsApi = {
  getDefinitions: () => api.get('/api/cards/definitions'),
  getUserCards: () => api.get('/api/cards/user'),
  synthesize: (cardId) => api.post('/api/cards/synthesize', { cardId })
}

export const achievementsApi = {
  getAll: () => api.get('/api/achievements/list'),
  getUser: () => api.get('/api/achievements/user')
}

export const userApi = {
  login: (openid) => api.post('/api/user/login', { openid }),
  getStats: () => api.get('/api/user/stats'),
  reset: () => api.post('/api/user/reset'),
  buyVip: () => api.post('/api/user/buy-vip')
}
