<template>
  <div class="home-view page-container">
    <div class="header">
      <div class="header-decoration">
        <div class="decoration-dot"></div>
        <div class="decoration-dot"></div>
        <div class="decoration-dot"></div>
      </div>
      <h1 class="title gold-gradient">言外</h1>
      <p class="subtitle">读懂TA的潜台词</p>
    </div>

    <div class="input-section">
      <div v-if="selectedImage" class="image-preview-wrapper">
        <img :src="selectedImage" alt="预览图片" class="preview-img" />
        <button class="btn-remove" @click="removeImage">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" width="20" height="20">
            <path d="M18 6L6 18M6 6l12 12" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
        <div class="image-status" v-if="analysisStore.isImageAnalyzing">
          <div class="spinner"></div>
          <span>识别中...</span>
        </div>
      </div>

      <div v-else class="upload-placeholder" @click="triggerUpload">
        <div class="upload-icon-wrapper">
          <div class="upload-icon">📷</div>
        </div>
        <p class="upload-text">点击上传聊天截图</p>
        <p class="upload-hint">支持识别微信、QQ等聊天界面</p>
      </div>

      <div class="image-progress-wrapper" v-if="analysisStore.isAnalyzing && analysisStore.analysisType === 'image'">
        <div class="image-progress-bar">
          <div class="image-progress-fill" :style="{width: analysisStore.loadingProgress + '%'}"></div>
        </div>
        <span class="image-progress-text">正在分析图片... {{ Math.round(analysisStore.loadingProgress) }}%</span>
      </div>
    </div>

    <div class="divider">
      <span class="divider-text">或者</span>
    </div>

    <div class="text-input-wrapper">
      <el-input
        v-model="dialogText"
        type="textarea"
        :rows="6"
        placeholder="粘贴对话记录...

例如：
A: 今天天气真好
B: 是啊
A: 我们出去走走吧
B: 随便"
        resize="none"
        class="dialog-input"
      />
    </div>

    <div class="count-wrapper">
      <div class="remaining-count" :class="{ vip: userStore.memberLevel === 1 }">
        <span class="count">{{ userStore.remainingAnalysis }}</span>
        <span class="label">/{{ dailyLimit }}次今日解码</span>
        <span v-if="userStore.memberLevel === 1" class="vip-tag">⭐会员</span>
      </div>
    </div>

    <div class="history-section" v-if="historyList.length > 0 || historyLoading">
      <div class="section-header">
        <span class="section-title">最近解码</span>
      </div>
      <div class="history-list" v-if="!historyLoading && historyList.length > 0">
        <div
          v-for="(item, index) in historyList.slice(0, 3)"
          :key="item.id"
          class="history-item"
        >
          <div class="history-content-wrapper" @click="loadHistoryDetail(item)">
            <div class="history-content">
              {{ (item.originalText || '').substring(0, 60) }}{{ item.originalText && item.originalText.length > 60 ? '...' : '' }}
            </div>
            <div class="history-footer">
              <span class="history-relationship">{{ item.relationship || '未分类' }}</span>
              <span class="history-time">{{ formatTime(item.createdAt) }}</span>
            </div>
          </div>
          <button class="delete-btn" @click.stop="deleteHistoryItem(item.id)">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" width="16" height="16">
              <path d="M3 6h18M19 8v11a2 2 0 01-2 2H6a2 2 0 01-2-2V8zm5 0v14M10 10v6M14 10v6M9 3h6" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
      </div>
      <div class="empty-history" v-if="!historyLoading && historyList.length === 0">
        <span class="empty-icon">📝</span>
        <p>暂无解码记录，开始你的第一次分析吧！</p>
      </div>
      <div class="history-loading" v-if="historyLoading">
        <div class="loading-dots">
          <span></span>
          <span></span>
          <span></span>
        </div>
      </div>
    </div>

    <button
      class="btn-gold decode-btn"
      :disabled="(!dialogText.trim() && !analysisStore.isImageAnalyzing) || !canDecode || analysisStore.isAnalyzing"
      @click="startDecode"
    >
      <span class="btn-content">
        <svg v-if="analysisStore.isAnalyzing && !analysisStore.isImageAnalyzing" class="btn-spinner" viewBox="0 0 24 24">
          <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="3" fill="none"/>
          <circle cx="12" cy="12" r="6" stroke="currentColor" stroke-width="4" fill="none"/>
        </svg>
        <span v-else>🔮</span>
        <span>{{ analysisStore.isAnalyzing ? '解码中...' : '开始解码' }}</span>
      </span>
    </button>

    <input
      ref="fileInput"
      type="file"
      accept="image/*"
      style="display: none"
      @change="handleFileSelect"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { useAnalysisStore } from '../stores/analysis'
import { analysisApi } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const analysisStore = useAnalysisStore()

const dialogText = ref('')
const historyList = ref([])
const historyLoading = ref(false)
const selectedImage = ref(null)
const fileInput = ref(null)

const canDecode = computed(() => {
  return userStore.remainingAnalysis > 0
})

const dailyLimit = computed(() => {
  return userStore.memberLevel === 1 ? 10 : 3
})

onMounted(() => {
  if (analysisStore.result && !analysisStore.isAnalyzing) {
    router.push({
      path: '/result',
      query: { timestamp: Date.now() }
    })
    return
  }
  if (analysisStore.originalImage && !analysisStore.isAnalyzing) {
    selectedImage.value = analysisStore.originalImage
  }
  loadHistory()
})

function triggerUpload() {
  if (analysisStore.isAnalyzing && analysisStore.analysisType === 'text') return
  fileInput.value?.click()
}

async function handleFileSelect(event) {
  const file = event.target.files?.[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    ElMessage.error('请上传图片文件')
    return
  }

  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过5MB')
    return
  }

  const reader = new FileReader()
  reader.onload = (e) => {
    selectedImage.value = e.target.result
  }
  reader.readAsDataURL(file)

  await processImage(file)
  event.target.value = ''
}

async function processImage(file) {
  if (!canDecode.value) {
    ElMessage.warning('今日免费解码次数已用完')
    removeImage()
    return
  }

  try {
    await analysisStore.startImageAnalysis(file)

    userStore.incrementDailyCount()

    router.push({
      path: '/result',
      query: { timestamp: Date.now() }
    })
  } catch (e) {
    console.error('Image analyze failed:', e)
    removeImage()
    ElMessage.error(analysisStore.error || '图片分析失败，请重试')
  }
}

function removeImage() {
  selectedImage.value = null
  analysisStore.originalImage = null
}

async function startDecode() {
  if (!dialogText.value.trim()) {
    ElMessage.warning('请输入对话内容')
    return
  }

  if (dialogText.value.length > 5000) {
    ElMessage.warning('对话内容过长，请限制在5000字以内')
    return
  }

  if (!canDecode.value) {
    ElMessage.warning('今日免费解码次数已用完')
    return
  }

  try {
    await analysisStore.startTextAnalysis(dialogText.value)

    userStore.incrementDailyCount()

    router.push({
      path: '/result',
      query: { timestamp: Date.now() }
    })
  } catch (e) {
    console.error('Decode failed:', e)
    ElMessage.error(analysisStore.error || '解码失败，请重试')
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
      dialogText.value = item.originalText || ''
    }
  } else {
    dialogText.value = item.originalText || ''
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

async function loadHistory() {
  historyLoading.value = true
  try {
    const res = await analysisApi.getHistory(1, 10)
    historyList.value = res.data.data.records || []
  } catch (e) {
    console.error('Load history failed:', e)
  } finally {
    historyLoading.value = false
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
  return date.toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.home-view {
  padding: 16px 16px 90px;
  position: relative;
}

.header {
  text-align: center;
  padding: 24px 0 28px;
  position: relative;
}

.header-decoration {
  position: absolute;
  top: 8px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.decoration-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: linear-gradient(135deg, #D4AF37, #F5D061);
  opacity: 0.4;
  animation: pulse 2s ease-in-out infinite;
}

.decoration-dot:nth-child(2) {
  animation-delay: 0.3s;
}

.decoration-dot:nth-child(3) {
  animation-delay: 0.6s;
}

.title {
  font-size: 48px;
  font-weight: 800;
  letter-spacing: 2px;
  margin-bottom: 6px;
  text-shadow: 0 0 30px rgba(212, 175, 55, 0.2);
}

.subtitle {
  color: var(--text-secondary);
  font-size: 15px;
  font-weight: 500;
  letter-spacing: 1px;
}

.input-section {
  margin-bottom: 20px;
}

.upload-placeholder {
  background: linear-gradient(145deg, rgba(30, 41, 59, 0.8), rgba(18, 24, 38, 0.9));
  border: 2px dashed rgba(212, 175, 55, 0.3);
  border-radius: 20px;
  padding: 40px 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  margin-bottom: 16px;
  position: relative;
  overflow: hidden;
}

.upload-placeholder::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at center, rgba(212, 175, 55, 0.1) 0%, transparent 70%);
  opacity: 0;
  transition: opacity 0.3s;
}

.upload-placeholder:hover {
  border-color: rgba(212, 175, 55, 0.6);
  background: linear-gradient(145deg, rgba(30, 41, 59, 0.95), rgba(24, 36, 56, 0.95));
  transform: translateY(-3px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.4);
}

.upload-placeholder:hover::before {
  opacity: 1;
}

.upload-icon-wrapper {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  border-radius: 24px;
  background: linear-gradient(135deg, rgba(184, 134, 11, 0.2), rgba(212, 175, 55, 0.1));
  border: 2px solid rgba(212, 175, 55, 0.2);
  margin-bottom: 16px;
  transition: all 0.3s;
}

.upload-placeholder:hover .upload-icon-wrapper {
  transform: scale(1.05);
  border-color: rgba(212, 175, 55, 0.4);
  box-shadow: 0 0 30px rgba(212, 175, 55, 0.3);
}

.upload-icon {
  font-size: 40px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

.upload-text {
  color: var(--text-primary);
  font-size: 17px;
  font-weight: 600;
  margin: 0 0 6px;
}

.upload-hint {
  color: var(--text-tertiary);
  font-size: 13px;
  margin: 0;
}

.image-preview-wrapper {
  background: var(--bg-card);
  border-radius: 20px;
  padding: 16px;
  margin-bottom: 16px;
  position: relative;
  border: 1px solid rgba(212, 175, 55, 0.15);
}

.preview-img {
  width: 100%;
  max-height: 240px;
  object-fit: contain;
  border-radius: 12px;
  display: block;
}

.btn-remove {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(239, 68, 68, 0.9);
  border: none;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  backdrop-filter: blur(4px);
}

.btn-remove:hover {
  transform: scale(1.1);
  background: rgba(239, 68, 68, 1);
  box-shadow: 0 4px 16px rgba(239, 68, 68, 0.4);
}

.image-status {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 12px;
  color: #D4AF37;
  font-size: 14px;
  font-weight: 500;
}

.spinner {
  width: 20px;
  height: 20px;
  border: 3px solid rgba(212, 175, 55, 0.2);
  border-top-color: #D4AF37;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.divider {
  display: flex;
  align-items: center;
  margin: 16px 0;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(212, 175, 55, 0.2), transparent);
}

.divider-text {
  padding: 0 16px;
  color: var(--text-tertiary);
  font-size: 13px;
}

.text-input-wrapper {
  margin-bottom: 12px;
}

.count-wrapper {
  margin-top: 12px;
}

.remaining-count {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: var(--text-secondary);
}

.remaining-count .count {
  color: #D4AF37;
  font-size: 22px;
  font-weight: 700;
  margin-right: 6px;
}

.remaining-count.vip .vip-badge {
  background: linear-gradient(135deg, #B8860B, #D4AF37, #F5D061);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-weight: 700;
  font-size: 15px;
}

.vip-tag {
  margin-left: 8px;
  font-size: 12px;
  color: #D4AF37;
}

.history-section {
  margin-bottom: 20px;
}

.section-header {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.section-title {
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 600;
  letter-spacing: 0.5px;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.history-item {
  background: var(--bg-card);
  border-radius: 16px;
  padding: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(212, 175, 55, 0.1);
}

.history-item:hover {
  border-color: rgba(212, 175, 55, 0.25);
  transform: translateY(-2px);
  box-shadow: var(--shadow-soft);
}

.history-content-wrapper {
  flex: 1;
  cursor: pointer;
}

.history-content {
  color: var(--text-primary);
  font-size: 14px;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
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
}

.history-time {
  color: var(--text-tertiary);
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
}

.delete-btn:hover {
  opacity: 1;
  background: rgba(239, 68, 68, 0.15);
  transform: scale(1.1);
}

.empty-history {
  color: var(--text-secondary);
  font-size: 14px;
  text-align: center;
  padding: 36px 20px;
  background: var(--bg-input);
  border-radius: 16px;
  border: 1px dashed rgba(212, 175, 55, 0.1);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.empty-icon {
  font-size: 40px;
  opacity: 0.6;
}

.empty-history p {
  margin: 0;
}

.history-loading {
  padding: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.loading-dots {
  display: flex;
  gap: 6px;
}

.loading-dots span {
  width: 8px;
  height: 8px;
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

.decode-btn {
  width: 100%;
  padding: 16px 28px;
  font-size: 17px;
  border-radius: 16px;
  margin-top: 8px;
  position: relative;
  overflow: hidden;
}

.btn-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-spinner {
  width: 20px;
  height: 20px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  backdrop-filter: blur(10px);
}

body.theme-light .loading-overlay {
  background: rgba(248, 250, 252, 0.95);
}

.loading-animation {
  position: relative;
}

.loading-animation .icon {
  font-size: 90px;
  animation: float 2s ease-in-out infinite;
  display: block;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-12px) rotate(5deg); }
}

.loading-text {
  margin-top: 24px;
  font-size: 18px;
  color: #D4AF37;
  font-weight: 500;
  letter-spacing: 1px;
  animation: pulse 1.8s ease-in-out infinite;
}

.loading-progress {
  width: 200px;
  margin-top: 24px;
}

.loading-progress .progress-bar {
  height: 6px;
  background: var(--bg-input);
  border-radius: 3px;
  overflow: hidden;
}

.loading-progress .progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #B8860B, #D4AF37, #F5D061);
  transition: width 0.3s ease;
  border-radius: 3px;
}

.image-progress-wrapper {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.image-progress-bar {
  width: 100%;
  height: 6px;
  background: rgba(212, 175, 55, 0.15);
  border-radius: 3px;
  overflow: hidden;
}

.image-progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #B8860B, #D4AF37, #F5D061);
  border-radius: 3px;
  transition: width 0.3s ease;
  box-shadow: 0 0 10px rgba(212, 175, 55, 0.4);
}

.image-progress-text {
  color: #D4AF37;
  font-size: 13px;
  font-weight: 500;
}
</style>
