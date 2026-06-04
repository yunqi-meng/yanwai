<template>
  <div class="analysis-container">
    <div class="page-header">
      <div class="page-title">
        <el-icon><Document /></el-icon>
        <span>分析记录</span>
      </div>
      <div class="page-desc">查看所有对话分析记录</div>
    </div>
    
    <div class="content-card">
      <div class="search-section">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="用户ID">
            <el-input v-model="searchForm.userId" placeholder="用户ID" clearable class="search-input-sm">
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="开始日期">
            <el-date-picker
              v-model="searchForm.startDate"
              type="date"
              placeholder="选择日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              class="date-picker"
            />
          </el-form-item>
          <el-form-item label="结束日期">
            <el-date-picker
              v-model="searchForm.endDate"
              type="date"
              placeholder="选择日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              class="date-picker"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch" class="search-btn">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset" class="reset-btn">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <div class="table-section">
        <el-table :data="analysisList" stripe v-loading="loading" class="custom-table">
          <el-table-column prop="id" label="ID" width="80">
            <template #default="{ row }">
              <span class="id-badge">#{{ row.id }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="userId" label="用户" width="120">
            <template #default="{ row }">
              <div class="user-cell">
                <el-avatar :size="32" class="user-avatar">
                  {{ row.userId }}
                </el-avatar>
                <span class="user-id">用户{{ row.userId }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="originalText" label="原始对话" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">
              <div class="text-cell">
                <span class="text-content">{{ row.originalText }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="relationship" label="关系" width="120">
            <template #default="{ row }">
              <el-tag effect="dark" round class="relation-tag" v-if="row.relationship">
                {{ row.relationship }}
              </el-tag>
              <span v-else class="empty-text">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="分析时间" width="180">
            <template #default="{ row }">
              <div class="time-cell">
                <el-icon><Clock /></el-icon>
                <span>{{ formatTime(row.createdAt) }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button type="primary" size="small" @click="handleViewDetail(row)" class="action-btn">
                  <el-icon><View /></el-icon>
                  详情
                </el-button>
                <el-popconfirm title="确定删除该记录吗？" @confirm="handleDelete(row.id)">
                  <template #reference>
                    <el-button type="danger" size="small" class="action-btn">
                      <el-icon><Delete /></el-icon>
                      删除
                    </el-button>
                  </template>
                </el-popconfirm>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>
    
    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="分析详情" width="850px" class="detail-dialog">
      <div class="detail-content">
        <div class="detail-info">
          <el-descriptions :column="2" border class="info-descriptions">
            <el-descriptions-item label="记录ID">
              <span class="id-badge">#{{ currentRecord.id }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="用户ID">
              <div class="user-cell">
                <el-avatar :size="28" class="user-avatar">{{ currentRecord.userId }}</el-avatar>
                <span>用户{{ currentRecord.userId }}</span>
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="关系" :span="2">
              <el-tag effect="dark" round v-if="currentRecord.relationship">
                {{ currentRecord.relationship }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="原始对话" :span="2">
              <div class="original-text">{{ currentRecord.originalText }}</div>
            </el-descriptions-item>
          </el-descriptions>
        </div>
        
        <div class="detail-result" v-if="parsedResult">
          <div class="result-section" v-if="parsedResult.emotionCurve && parsedResult.emotionCurve.length > 0">
            <div class="section-title">
              <el-icon><TrendCharts /></el-icon>
              <span>情绪曲线</span>
            </div>
            <div ref="emotionChartRef" class="emotion-chart"></div>
          </div>
          
          <div class="result-section" v-if="parsedResult.translations && parsedResult.translations.length > 0">
            <div class="section-title">
              <el-icon><ChatDotRound /></el-icon>
              <span>逐句翻译</span>
            </div>
            <div class="translation-list">
              <div v-for="(item, index) in parsedResult.translations" :key="index" class="translation-item">
                <div class="translation-header">
                  <span class="translation-index">{{ index + 1 }}</span>
                  <span class="translation-original">{{ item.original }}</span>
                </div>
                <div class="translation-body">
                  <div class="translation-row">
                    <span class="label">字面意思：</span>
                    <span class="value">{{ item.literal }}</span>
                  </div>
                  <div class="translation-row">
                    <span class="label">潜台词：</span>
                    <span class="value subtext">{{ item.subtext }}</span>
                  </div>
                  <div class="translation-row" v-if="item.emotionScore !== undefined">
                    <span class="label">情绪值：</span>
                    <el-progress 
                      :percentage="Math.round(item.emotionScore * 100)" 
                      :color="getEmotionColor(item.emotionScore)"
                      :stroke-width="8"
                      class="emotion-progress"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div class="result-section" v-if="parsedResult.advice && parsedResult.advice.length > 0">
            <div class="section-title">
              <el-icon><Promotion /></el-icon>
              <span>沟通建议</span>
            </div>
            <div class="advice-list">
              <div v-for="(item, index) in parsedResult.advice" :key="index" class="advice-item">
                <el-icon><CircleCheck /></el-icon>
                <span>{{ item }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick, computed } from 'vue'
import * as echarts from 'echarts'
import { getAnalysisList, getAnalysisDetail, deleteAnalysis } from '@/api/analysis'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const analysisList = ref([])
const detailDialogVisible = ref(false)
const currentRecord = ref({})
const emotionChartRef = ref(null)
let emotionChart = null

const searchForm = reactive({
  userId: '',
  startDate: '',
  endDate: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const parsedResult = computed(() => {
  if (!currentRecord.value.analysisResult) return null
  try {
    return JSON.parse(currentRecord.value.analysisResult)
  } catch {
    return null
  }
})

onMounted(() => {
  loadAnalysisList()
})

onUnmounted(() => {
  if (emotionChart) emotionChart.dispose()
})

async function loadAnalysisList() {
  loading.value = true
  try {
    const res = await getAnalysisList({
      page: pagination.page,
      size: pagination.size,
      userId: searchForm.userId || undefined,
      startDate: searchForm.startDate || undefined,
      endDate: searchForm.endDate || undefined
    })
    analysisList.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error('加载分析记录失败:', error)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  loadAnalysisList()
}

function handleReset() {
  searchForm.userId = ''
  searchForm.startDate = ''
  searchForm.endDate = ''
  pagination.page = 1
  loadAnalysisList()
}

function handleSizeChange(size) {
  pagination.size = size
  pagination.page = 1
  loadAnalysisList()
}

function handlePageChange(page) {
  pagination.page = page
  loadAnalysisList()
}

async function handleViewDetail(row) {
  currentRecord.value = row
  detailDialogVisible.value = true
  
  await nextTick()
  renderEmotionChart()
}

function renderEmotionChart() {
  if (!emotionChartRef.value || !parsedResult.value?.emotionCurve) return
  
  if (emotionChart) emotionChart.dispose()
  emotionChart = echarts.init(emotionChartRef.value)
  
  const data = parsedResult.value.emotionCurve
  
  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      borderColor: '#e8e8e8',
      borderWidth: 1,
      textStyle: {
        color: '#333'
      }
    },
    grid: {
      top: 30,
      right: 20,
      bottom: 30,
      left: 50
    },
    xAxis: {
      type: 'category',
      data: data.map((_, i) => `第${i + 1}句`),
      axisLine: {
        lineStyle: {
          color: '#e8e8e8'
        }
      },
      axisLabel: {
        color: '#666'
      }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 1,
      name: '情绪值',
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      splitLine: {
        lineStyle: {
          color: '#f0f0f0'
        }
      },
      axisLabel: {
        color: '#666'
      }
    },
    series: [{
      data: data.map(item => item.emotionScore),
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 10,
      lineStyle: {
        color: '#667eea',
        width: 3
      },
      itemStyle: {
        color: '#667eea',
        borderColor: '#fff',
        borderWidth: 2
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [{
            offset: 0,
            color: 'rgba(102, 126, 234, 0.3)'
          }, {
            offset: 1,
            color: 'rgba(102, 126, 234, 0.02)'
          }]
        }
      }
    }]
  }
  
  emotionChart.setOption(option)
}

async function handleDelete(id) {
  try {
    await deleteAnalysis(id)
    ElMessage.success('删除成功')
    loadAnalysisList()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

function formatTime(time) {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 19)
}

function getEmotionColor(score) {
  if (score >= 0.7) return '#67C23A'
  if (score >= 0.4) return '#E6A23C'
  return '#F56C6C'
}
</script>

<style scoped>
.analysis-container {
  padding: 0;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
}

.page-title .el-icon {
  color: #667eea;
  font-size: 28px;
}

.page-desc {
  margin-top: 8px;
  font-size: 14px;
  color: #999;
}

.content-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
}

.search-section {
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.search-form {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.search-input-sm {
  width: 160px;
}

.date-picker {
  width: 160px;
}

.search-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.search-btn:hover {
  opacity: 0.9;
}

.reset-btn {
  border-color: #dcdfe6;
}

.table-section {
  margin-bottom: 24px;
}

.custom-table {
  border-radius: 12px;
  overflow: hidden;
}

.custom-table :deep(.el-table__header th) {
  background: #f5f7fa !important;
  color: #666;
  font-weight: 600;
  border-bottom: none;
}

.custom-table :deep(.el-table__row) {
  transition: all 0.3s ease;
}

.custom-table :deep(.el-table__row:hover) {
  background: #f5f7fa !important;
}

.custom-table :deep(.el-table__row td) {
  border-bottom: 1px solid #f0f0f0;
}

.id-badge {
  display: inline-block;
  padding: 4px 10px;
  background: #f0f2f5;
  border-radius: 8px;
  font-size: 13px;
  color: #666;
  font-weight: 600;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-weight: 600;
  font-size: 12px;
}

.user-id {
  font-size: 13px;
  color: #666;
}

.text-cell {
  max-width: 300px;
}

.text-content {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-size: 13px;
  color: #333;
  line-height: 1.5;
}

.relation-tag {
  padding: 6px 14px;
  font-size: 13px;
}

.empty-text {
  color: #999;
}

.time-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #999;
  font-size: 13px;
}

.time-cell .el-icon {
  font-size: 14px;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 6px;
}

.action-btn {
  font-size: 12px;
  padding: 5px 10px;
  height: 28px;
}

.action-btn .el-icon {
  font-size: 14px;
}

.pagination-section {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

/* 详情弹窗 */
.detail-dialog :deep(.el-dialog) {
  border-radius: 16px;
}

.detail-dialog :deep(.el-dialog__header) {
  padding: 24px 24px 16px;
  border-bottom: 1px solid #f0f0f0;
  margin: 0;
}

.detail-dialog :deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
}

.detail-dialog :deep(.el-dialog__body) {
  padding: 24px;
  max-height: 70vh;
  overflow-y: auto;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.info-descriptions {
  border-radius: 12px;
  overflow: hidden;
}

.info-descriptions :deep(.el-descriptions__label) {
  background: #f5f7fa !important;
  font-weight: 600;
}

.original-text {
  white-space: pre-wrap;
  word-break: break-all;
  line-height: 1.6;
  color: #333;
}

.result-section {
  background: #f5f7fa;
  border-radius: 12px;
  padding: 20px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 16px;
}

.section-title .el-icon {
  color: #667eea;
  font-size: 20px;
}

.emotion-chart {
  height: 200px;
  background: #fff;
  border-radius: 8px;
}

.translation-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.translation-item {
  background: #fff;
  border-radius: 10px;
  padding: 16px;
  border-left: 4px solid #667eea;
}

.translation-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.translation-index {
  width: 28px;
  height: 28px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
}

.translation-original {
  font-weight: 600;
  color: #1a1a2e;
  font-size: 15px;
}

.translation-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.translation-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  font-size: 14px;
}

.translation-row .label {
  color: #999;
  flex-shrink: 0;
  min-width: 80px;
}

.translation-row .value {
  color: #333;
  line-height: 1.5;
}

.translation-row .subtext {
  color: #667eea;
  font-weight: 500;
}

.emotion-progress {
  width: 200px;
}

.advice-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.advice-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  background: #fff;
  padding: 14px;
  border-radius: 10px;
  font-size: 14px;
  color: #333;
  line-height: 1.5;
}

.advice-item .el-icon {
  color: #67C23A;
  font-size: 18px;
  flex-shrink: 0;
  margin-top: 2px;
}
</style>
