import request from '@/utils/request'

export function getAnalysisList(params) {
  return request.get('/analysis', { params })
}

export function getAnalysisDetail(id) {
  return request.get(`/analysis/${id}`)
}

export function deleteAnalysis(id) {
  return request.delete(`/analysis/${id}`)
}
