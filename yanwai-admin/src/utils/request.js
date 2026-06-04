import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api/admin',
  timeout: 30000
})

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('admin_token')
    const adminId = localStorage.getItem('admin_id')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    if (adminId) {
      config.headers['X-Admin-Id'] = adminId
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res
    } else {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        localStorage.removeItem('admin_token')
        localStorage.removeItem('admin_id')
        router.push('/login')
      }
      return Promise.reject(new Error(res.message))
    }
  },
  error => {
    if (error.response) {
      switch (error.response.status) {
        case 401:
          localStorage.removeItem('admin_token')
          localStorage.removeItem('admin_id')
          router.push('/login')
          break
        case 403:
          ElMessage.error('没有权限')
          break
        case 500:
          ElMessage.error('服务器错误')
          break
        default:
          ElMessage.error(error.message || '网络错误')
      }
    } else {
      ElMessage.error('网络连接失败')
    }
    return Promise.reject(error)
  }
)

export default request
