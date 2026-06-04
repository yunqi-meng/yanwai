<template>
  <div class="history-view page-container">
    <div class="header">
      <button class="back-btn" @click="$router.back()">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" width="24" height="24">
          <path d="M15 18l-6-6 6-6" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
      <h1 class="title">解码历史</h1>
      <span class="count">{{ historyTotal }}条</span>
    </div>

    <div class="history-list" v-if="!historyLoading && historyList.length > 0">
      <div
        v-for="(item, index) in historyList"
        :key="item.id"
        class="history-item"
      >
        <div class="history-content-wrapper" @click="loadHistoryDetail(item)">
          <div class="history-content">
            {{ item.originalText || '无内容' }}
          </div>
          <div class="history-footer">
            <span class="history-relationship">{{ item.relationship || '未分类' }}</span>
            <span class="history-time">{{ formatTime(item.createdAt) }}</span>
          </div>
        </div>
        <button class="delete-btn" @click.stop="deleteHistoryItem(item.id)">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" width="18" height="18">
            <path d="M3 6h18M19 8v11a2 2 0 01-2 2H6a2 2 0 01-2-2V8zm5 0v14M10 10v6M14 10v6M9 3h6" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
      </div>
    </div>

    <div class="load-more-wrapper" v-if="!historyLoading && historyList.length > 0 && historyList.length < historyTotal">
      <button class="load-more-btn" @click="loadMoreHistory">
        {{ loadingMore ? '加载中...' : '加载更多' }}
      </button>
    </div>

    <div class="empty-history" v-if="!historyLoading && historyList.length === 0">
      <span class="empty-icon">📝</span>
      <p>暂无解码记录</p>
    </div>

    <div class="history-loading" v-if="historyLoading">
      <div class="loading-dots">
        <span></span>
        <span></span>
        <span></span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { analysisApi } from '../api'
import { useAnalysisStore } from '../stores/analysis'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const analysisStore = useAnalysisStore()
const historyList = ref([])
const historyLoading = ref(false)
const loadingMore = ref(false)
const historyPage = ref(1)
const historyTotal = ref(0)
const historyPageSize = ref(20)

async function loadHistory() {
  historyLoading.value = true
  try {
    const res = await analysisApi.getHistory(historyPage.value, historyPageSize.value)
    historyList.value = res.data.data.records || []
    historyTotal.value = res.data.data.total || 0
  } catch (e) {
    console.error('Load history failed:', e)
  } finally {
    historyLoading.value = false
  }
}

async function loadMoreHistory() {
  if (loadingMore.value || historyList.value.length >= historyTotal.value) return
  
  loadingMore.value = true
  historyPage.value++
  try {
    const res = await analysisApi.getHistory(historyPage.value, historyPageSize.value)
    const newRecords = res.data.data.records || []
    historyList.value = [...historyList.value, ...newRecords]
  } catch (e) {
    console.error('Load more history failed:', e)
    historyPage.value--
  } finally {
    loadingMore.value = false
  }
}

async function loadHistoryDetail(item) {
  if (item.analysisResult) {
    try {
      const detailRes = await analysisApi.getHistoryDetail(item.id)
      const detail = detailRes.data.data
      const analysis = JSON.parse(detail.analysisResult)
      analysisStore.result = analysis
      analysisStore.newCard = null
      analysisStore.newAchievements = []
      analysisStore.originalImage = detail.originalImage || null
      analysisStore.analysisText = detail.originalText || ''
      router.push({
        path: '/result',
        query: { timestamp: Date.now() }
      })
    } catch (e) {
      console.error('Load analysis detail failed:', e)
    }
  }
}

async function deleteHistoryItem(id) {
  try {
    await ElMessageBox.confirm('确定要删除这条解码记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await analysisApi.deleteHistory(id)
    ElMessage.success('删除成功')
    await loadHistory()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('Delete history failed:', e)
      ElMessage.error('删除失败，请重试')
    }
  }
}

function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 86400000 * 7) return Math.floor(diff / 86400000) + '天前'
  return date.toLocaleDateString('zh-CN')
}

onMounted(async () => {
  await loadHistory()
})
</script>

<style scoped>
.history-view {
  padding: 16px 16px 100px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0 20px;
  position: relative;
}

.back-btn {
  background: transparent;
  border: none;
  color: #94A3B8;
  cursor: pointer;
  padding: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  border-radius: 12px;
}

.back-btn:hover {
  background: rgba(30, 41, 59, 0.8);
  color: #D4AF37;
}

.title {
  font-size: 20px;
  font-weight: 700;
  color: #F8FAFC;
  margin: 0;
}

.count {
  font-size: 14px;
  color: #64748B;
  width: 52px;
  text-align: right;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-item {
  background: linear-gradient(145deg, rgba(18, 24, 38, 0.95), rgba(18, 24, 38, 0.9));
  border-radius: 16px;
  padding: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(212, 175, 55, 0.1);
}

.history-item:hover {
  border-color: rgba(212, 175, 55, 0.25);
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
}

.history-content-wrapper {
  flex: 1;
  cursor: pointer;
  min-width: 0;
}

.history-content {
  color: #F8FAFC;
  font-size: 15px;
  line-height: 1.5;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  word-break: break-all;
}

.history-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.history-relationship {
  color: #D4AF37;
  font-size: 12px;
  font-weight: 500;
  padding: 4px 10px;
  background: rgba(212, 175, 55, 0.1);
  border-radius: 12px;
  flex-shrink: 0;
}

.history-time {
  color: #64748B;
  font-size: 12px;
  flex-shrink: 0;
}

.delete-btn {
  background: transparent;
  border: none;
  color: #EF4444;
  padding: 10px;
  border-radius: 10px;
  cursor: pointer;
  opacity: 0.6;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-left: 12px;
}

.delete-btn:hover {
  opacity: 1;
  background: rgba(239, 68, 68, 0.15);
  transform: scale(1.1);
}

.load-more-wrapper {
  margin-top: 16px;
  text-align: center;
}

.load-more-btn {
  background: rgba(212, 175, 55, 0.1);
  border: 1px solid rgba(212, 175, 55, 0.2);
  color: #D4AF37;
  padding: 12px 32px;
  border-radius: 12px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.load-more-btn:hover:not(:disabled) {
  background: rgba(212, 175, 55, 0.2);
  border-color: rgba(212, 175, 55, 0.3);
}

.load-more-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.empty-history {
  color: #94A3B8;
  font-size: 14px;
  text-align: center;
  padding: 60px 20px;
  background: rgba(30, 41, 59, 0.4);
  border-radius: 16px;
  border: 1px dashed rgba(212, 175, 55, 0.1);
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 40px;
}

.empty-icon {
  font-size: 48px;
  opacity: 0.6;
}

.empty-history p {
  margin: 0;
}

.history-loading {
  padding: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.loading-dots {
  display: flex;
  gap: 8px;
}

.loading-dots span {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #D4AF37;
  opacity: 0.3;
  animation: loadingPulse 1.4s ease-in-out infinite;
}

.loading-dots span:nth-child(2) {
  animation-delay: 0.2s;
}

.loading-dots span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes loadingPulse {
  0%, 100% {
    opacity: 0.3;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.3);
  }
}
</style>
