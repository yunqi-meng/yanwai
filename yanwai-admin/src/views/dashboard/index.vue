<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <div class="stat-card stat-card-primary">
          <div class="stat-card-inner">
            <div class="stat-icon-wrapper">
              <el-icon :size="28"><User /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ overview.totalUsers || 0 }}</div>
              <div class="stat-label">总用户数</div>
            </div>
          </div>
          <div class="stat-footer">
            <span class="stat-trend up">
              <el-icon><Top /></el-icon>
              12%
            </span>
            <span class="stat-period">较上周</span>
          </div>
        </div>
      </el-col>
      
      <el-col :span="6">
        <div class="stat-card stat-card-success">
          <div class="stat-card-inner">
            <div class="stat-icon-wrapper">
              <el-icon :size="28"><UserFilled /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ overview.todayNewUsers || 0 }}</div>
              <div class="stat-label">今日新增</div>
            </div>
          </div>
          <div class="stat-footer">
            <span class="stat-trend up">
              <el-icon><Top /></el-icon>
              8%
            </span>
            <span class="stat-period">较昨日</span>
          </div>
        </div>
      </el-col>
      
      <el-col :span="6">
        <div class="stat-card stat-card-warning">
          <div class="stat-card-inner">
            <div class="stat-icon-wrapper">
              <el-icon :size="28"><Document /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ overview.totalAnalysis || 0 }}</div>
              <div class="stat-label">总分析次数</div>
            </div>
          </div>
          <div class="stat-footer">
            <span class="stat-trend up">
              <el-icon><Top /></el-icon>
              15%
            </span>
            <span class="stat-period">较上周</span>
          </div>
        </div>
      </el-col>
      
      <el-col :span="6">
        <div class="stat-card stat-card-danger">
          <div class="stat-card-inner">
            <div class="stat-icon-wrapper">
              <el-icon :size="28"><DataAnalysis /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ overview.todayAnalysis || 0 }}</div>
              <div class="stat-label">今日分析</div>
            </div>
          </div>
          <div class="stat-footer">
            <span class="stat-trend up">
              <el-icon><Top /></el-icon>
              5%
            </span>
            <span class="stat-period">较昨日</span>
          </div>
        </div>
      </el-col>
    </el-row>
    
    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <div class="chart-title">
              <el-icon><TrendCharts /></el-icon>
              <span>用户增长趋势</span>
            </div>
            <div class="chart-actions">
              <el-radio-group v-model="userChartDays" size="small">
                <el-radio-button label="7">近7天</el-radio-button>
                <el-radio-button label="30">近30天</el-radio-button>
              </el-radio-group>
            </div>
          </div>
          <div ref="userChartRef" class="chart-container"></div>
        </div>
      </el-col>
      
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <div class="chart-title">
              <el-icon><DataLine /></el-icon>
              <span>分析次数趋势</span>
            </div>
            <div class="chart-actions">
              <el-radio-group v-model="analysisChartDays" size="small">
                <el-radio-button label="7">近7天</el-radio-button>
                <el-radio-button label="30">近30天</el-radio-button>
              </el-radio-group>
            </div>
          </div>
          <div ref="analysisChartRef" class="chart-container"></div>
        </div>
      </el-col>
    </el-row>
    
    <!-- 数据表格 -->
    <el-row :gutter="20">
      <el-col :span="16">
        <div class="table-card">
          <div class="table-header">
            <div class="table-title">
              <el-icon><Document /></el-icon>
              <span>最近分析记录</span>
            </div>
            <el-button type="primary" link>
              查看全部
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
          <el-table :data="hotAnalysis" stripe class="custom-table">
            <el-table-column prop="id" label="ID" width="80">
              <template #default="{ row }">
                <span class="id-badge">#{{ row.id }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="nickname" label="用户" width="120">
              <template #default="{ row }">
                <div class="user-cell">
                  <el-avatar :size="28" class="user-cell-avatar">
                    {{ row.nickname?.charAt(0) || 'U' }}
                  </el-avatar>
                  <span>{{ row.nickname }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="originalText" label="原始对话" show-overflow-tooltip />
            <el-table-column prop="relationship" label="关系" width="120">
              <template #default="{ row }">
                <el-tag effect="plain" round>{{ row.relationship || '-' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="分析时间" width="160">
              <template #default="{ row }">
                <span class="time-text">{{ formatTime(row.createdAt) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
      
      <el-col :span="8">
        <div class="info-card">
          <div class="info-header">
            <div class="info-title">
              <el-icon><InfoFilled /></el-icon>
              <span>系统信息</span>
            </div>
          </div>
          <div class="info-list">
            <div class="info-item">
              <span class="info-label">会员用户</span>
              <span class="info-value">{{ overview.memberUsers || 0 }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">卡牌总数</span>
              <span class="info-value">{{ overview.totalCards || 0 }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">成就总数</span>
              <span class="info-value">{{ overview.totalAchievements || 0 }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">系统版本</span>
              <span class="info-value">v1.0.0</span>
            </div>
            <div class="info-item">
              <span class="info-label">运行状态</span>
              <span class="info-value">
                <el-tag type="success" effect="dark" size="small">正常</el-tag>
              </span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getDashboardOverview, getDashboardTrend, getHotAnalysis } from '@/api/dashboard'

const overview = ref({})
const hotAnalysis = ref([])
const trendData = ref(null)
const userChartRef = ref(null)
const analysisChartRef = ref(null)
const userChartDays = ref('7')
const analysisChartDays = ref('7')

let userChart = null
let analysisChart = null

onMounted(async () => {
  await nextTick()
  initCharts()
  await loadData()
  renderCharts()
})

onUnmounted(() => {
  if (userChart) userChart.dispose()
  if (analysisChart) analysisChart.dispose()
  window.removeEventListener('resize', handleResize)
})

watch(userChartDays, async () => {
  await loadTrendData()
  renderCharts()
})

watch(analysisChartDays, async () => {
  await loadTrendData()
  renderCharts()
})

async function loadData() {
  try {
    const [overviewRes, hotRes] = await Promise.all([
      getDashboardOverview(),
      getHotAnalysis(10)
    ])

    overview.value = overviewRes.data
    hotAnalysis.value = hotRes.data
  } catch (error) {
    console.error('加载概览数据失败:', error)
  }
  await loadTrendData()
}

async function loadTrendData() {
  try {
    const res = await getDashboardTrend(Number(userChartDays.value))
    trendData.value = res.data
  } catch (error) {
    console.error('加载趋势数据失败:', error)
  }
}

function initCharts() {
  if (userChartRef.value) {
    userChart?.dispose()
    userChart = echarts.init(userChartRef.value)
  }
  if (analysisChartRef.value) {
    analysisChart?.dispose()
    analysisChart = echarts.init(analysisChartRef.value)
  }

  window.addEventListener('resize', handleResize)
}

function renderCharts() {
  if (trendData.value) {
    renderUserChart(trendData.value.userTrend || [])
    renderAnalysisChart(trendData.value.analysisTrend || [])
  }
}

function handleResize() {
  userChart?.resize()
  analysisChart?.resize()
}

function renderUserChart(data) {
  if (!userChart) return
  
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
      data: data.map(item => item.date),
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
      data: data.map(item => item.count),
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
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
  
  userChart.setOption(option)
}

function renderAnalysisChart(data) {
  if (!analysisChart) return
  
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
      data: data.map(item => item.date),
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
      data: data.map(item => item.count),
      type: 'bar',
      barWidth: 30,
      itemStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [{
            offset: 0,
            color: '#67C23A'
          }, {
            offset: 1,
            color: '#95d475'
          }]
        },
        borderRadius: [6, 6, 0, 0]
      }
    }]
  }
  
  analysisChart.setOption(option)
}

function formatTime(time) {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 19)
}
</script>

<style scoped>
.dashboard {
  padding: 0;
}

/* 统计卡片 */
.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
}

.stat-card-primary::before {
  background: linear-gradient(90deg, #667eea, #764ba2);
}

.stat-card-success::before {
  background: linear-gradient(90deg, #67C23A, #95d475);
}

.stat-card-warning::before {
  background: linear-gradient(90deg, #E6A23C, #f0c78a);
}

.stat-card-danger::before {
  background: linear-gradient(90deg, #F56C6C, #f89898);
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
}

.stat-card-inner {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}

.stat-icon-wrapper {
  width: 60px;
  height: 60px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-card-primary .stat-icon-wrapper {
  background: linear-gradient(135deg, #667eea, #764ba2);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.stat-card-success .stat-icon-wrapper {
  background: linear-gradient(135deg, #67C23A, #95d475);
  box-shadow: 0 8px 20px rgba(103, 194, 58, 0.4);
}

.stat-card-warning .stat-icon-wrapper {
  background: linear-gradient(135deg, #E6A23C, #f0c78a);
  box-shadow: 0 8px 20px rgba(230, 162, 60, 0.4);
}

.stat-card-danger .stat-icon-wrapper {
  background: linear-gradient(135deg, #F56C6C, #f89898);
  box-shadow: 0 8px 20px rgba(245, 108, 108, 0.4);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 4px;
}

.stat-footer {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 600;
}

.stat-trend.up {
  color: #67C23A;
}

.stat-trend.down {
  color: #F56C6C;
}

.stat-period {
  font-size: 12px;
  color: #999;
}

/* 图表卡片 */
.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
}

.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.chart-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
}

.chart-title .el-icon {
  color: #667eea;
  font-size: 20px;
}

.chart-container {
  height: 300px;
}

/* 表格卡片 */
.table-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.table-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
}

.table-title .el-icon {
  color: #667eea;
  font-size: 20px;
}

.custom-table :deep(.el-table__header th) {
  background: #f5f7fa !important;
  color: #666;
  font-weight: 600;
}

.custom-table :deep(.el-table__row) {
  transition: all 0.3s ease;
}

.custom-table :deep(.el-table__row:hover) {
  background: #f5f7fa !important;
}

.id-badge {
  display: inline-block;
  padding: 2px 8px;
  background: #f0f2f5;
  border-radius: 6px;
  font-size: 12px;
  color: #666;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-cell-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-size: 12px;
}

.time-text {
  color: #999;
  font-size: 13px;
}

/* 信息卡片 */
.info-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
}

.info-header {
  margin-bottom: 20px;
}

.info-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
}

.info-title .el-icon {
  color: #667eea;
  font-size: 20px;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 10px;
  transition: all 0.3s ease;
}

.info-item:hover {
  background: #e8eaed;
}

.info-label {
  font-size: 14px;
  color: #666;
}

.info-value {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a2e;
}
</style>
