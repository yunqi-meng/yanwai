import request from '@/utils/request'

export function getDashboardOverview() {
  return request.get('/dashboard/overview')
}

export function getDashboardTrend(days = 7) {
  return request.get('/dashboard/trend', { params: { days } })
}

export function getHotAnalysis(limit = 10) {
  return request.get('/dashboard/hot', { params: { limit } })
}
