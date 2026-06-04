import axios from 'axios'

const baseURL = import.meta.env.VITE_API_BASE_URL || ''

const api = axios.create({
  baseURL: baseURL,
  timeout: 180000,
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
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return response
  },
  error => {
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
  analyzeImage: (formData) => api.post('/api/analysis/analyze-image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 180000
  }),
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
  getUserCards: () => api.get('/api/cards/user')
}

export const achievementsApi = {
  getAll: () => api.get('/api/achievements/list'),
  getUser: () => api.get('/api/achievements/user')
}

export const userApi = {
  login: (email, password) => api.post('/api/user/login', { email, password }),
  register: (email, password) => api.post('/api/user/register', { email, password }),
  guestLogin: (deviceId) => api.post('/api/user/guest-login', { deviceId }),
  getStats: () => api.get('/api/user/stats'),
  reset: () => api.post('/api/user/reset'),
  buyVip: () => api.post('/api/user/buy-vip'),
  watchAdComplete: () => api.post('/api/user/watch-ad-complete'),
  canWatchAd: () => api.get('/api/user/can-watch-ad'),
  updateInfo: (nickname, avatar) => api.post('/api/user/update-info', { nickname, avatar })
}
