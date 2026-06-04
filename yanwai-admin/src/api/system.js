import request from '@/utils/request'

// 系统配置
export function getAllConfigs() {
  return request.get('/config')
}

export function updateConfig(data) {
  return request.put('/config', data)
}

// 操作日志
export function getLogList(params) {
  return request.get('/logs', { params })
}
