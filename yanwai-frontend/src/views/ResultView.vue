<template>
  <div class="result-view page-container" ref="resultContainer">
    <div class="header">
      <button class="back-btn" @click="goHome">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" width="24" height="24">
          <path d="M15 18l-6-6 6-6" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
      <div class="header-decoration">
        <div class="decoration-dot"></div>
        <div class="decoration-dot"></div>
        <div class="decoration-dot"></div>
      </div>
      <h1 class="title gold-gradient">解码结果</h1>
    </div>

    <div v-if="originalImage" class="image-section card">
      <button class="view-image-btn" @click="openImageModal">
        <span class="btn-icon">🖼️</span>
        <span>查看原图</span>
      </button>
    </div>

    <div class="relationship-section card">
      <div class="section-label">关系识别</div>
      <div class="relationship-tag">{{ analysis?.relationship || '未识别' }}</div>
    </div>

    <div class="emotion-section card" v-if="analysis?.emotionCurve?.length">
      <div class="section-label">情绪曲线</div>
      <div ref="chartRef" class="chart-container"></div>
    </div>

    <div class="translations-section">
      <div class="section-title">逐句翻译</div>
      <div
        v-for="(item, index) in analysis?.translations"
        :key="index"
        class="translation-card card"
      >
        <div class="original-text">{{ item.original }}</div>
        <div class="translation-content">
          <div class="translation-row">
            <span class="label">字面意思：</span>
            <span class="value">{{ item.literal }}</span>
          </div>
          <div class="translation-row subtext">
            <span class="label">潜台词：</span>
            <span class="value gold-text">{{ item.subtext }}</span>
          </div>
        </div>
        <div class="emotion-indicator">
          <div class="emotion-bar">
            <div
              class="emotion-fill"
              :style="{ width: (item.emotionScore * 100) + '%' }"
              :class="getEmotionClass(item.emotionScore)"
            ></div>
          </div>
          <span class="emotion-value">{{ getEmotionLabel(item.emotionScore) }}</span>
        </div>
      </div>
    </div>

    <div class="advice-section card" v-if="analysis?.advice?.length">
      <div class="section-label">沟通建议</div>
      <ul class="advice-list">
        <li v-for="(advice, index) in analysis.advice" :key="index">
          {{ advice }}
        </li>
      </ul>
    </div>

    <div class="reply-section card">
      <div class="section-header">
        <div class="section-title">
          <span class="title-icon">💬</span>
          回复模板
        </div>
        <div class="section-subtitle">点击即可复制</div>
      </div>
      <div class="reply-tabs">
        <div
          v-for="tab in replyTabs"
          :key="tab.id"
          class="reply-tab"
          :class="{ active: activeTab === tab.id }"
          @click="activeTab = tab.id"
        >
          <span class="tab-icon">{{ tab.icon }}</span>
          <span class="tab-name">{{ tab.name }}</span>
        </div>
      </div>
      <div class="reply-list">
        <div
          v-for="(reply, index) in currentReplies"
          :key="index"
          class="reply-item"
          @click="copyReply(reply)"
        >
          <div class="reply-content">
            <div class="reply-header">
              <span class="reply-label">{{ reply.label }}</span>
              <span class="reply-emotion" :class="reply.emotionClass">
                {{ reply.emotion }}
              </span>
            </div>
            <div class="reply-text">{{ reply.text }}</div>
          </div>
          <div class="copy-btn">
            <span class="copy-icon">{{ copiedIndex === index ? '✓' : '📋' }}</span>
          </div>
        </div>
      </div>
    </div>

    <button class="btn-gold share-btn" @click="generateShareImage">
      生成分享图
    </button>

    <div class="modal-overlay" v-if="showCardModal" @click.self="closeCardModal">
      <div class="card-modal" :class="'rarity-' + newCard?.rarity">
        <div class="card-icon">{{ newCard?.emoji }}</div>
        <div class="card-name">{{ newCard?.name }}</div>
        <div class="card-rarity">{{ getRarityName(newCard?.rarity) }}</div>
        <div class="card-description">{{ newCard?.description }}</div>
        <button class="btn-gold close-btn" @click="closeCardModal">收下</button>
      </div>
    </div>

    <div class="modal-overlay" v-if="showAchievementModal" @click.self="closeAchievementModal">
      <div class="achievement-modal">
        <div class="modal-title">🎉 新成就解锁</div>
        <div
          v-for="achievement in newAchievements"
          :key="achievement.id"
          class="achievement-item"
        >
          <div class="achievement-icon">{{ achievement.icon }}</div>
          <div class="achievement-info">
            <div class="achievement-name">{{ achievement.name }}</div>
            <div class="achievement-desc">{{ achievement.description }}</div>
          </div>
        </div>
        <button class="btn-gold close-btn" @click="closeAchievementModal">确定</button>
      </div>
    </div>

    <div class="modal-overlay" v-if="showImageModal" @click.self="closeImageModal">
      <div class="image-modal">
        <div class="modal-header">
          <span class="modal-title">原图预览</span>
          <button class="close-icon" @click="closeImageModal">✕</button>
        </div>
        <div class="image-container">
          <img :src="originalImage" alt="解码原图" class="preview-image" />
        </div>
        <button class="btn-gold download-btn" @click="downloadImage">
          <span>📥</span>
          <span>下载图片</span>
        </button>
      </div>
    </div>

    <canvas ref="confettiCanvas" class="confetti-canvas"></canvas>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import html2canvas from 'html2canvas'
import confetti from 'canvas-confetti'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { analysisApi } from '../api'
import { useAnalysisStore } from '../stores/analysis'

const router = useRouter()
const analysisStore = useAnalysisStore()
const resultContainer = ref(null)
const chartRef = ref(null)
const confettiCanvas = ref(null)
const copiedIndex = ref(-1)
const activeTab = ref('gentle')

const analysis = ref(null)
const newCard = ref(null)
const newAchievements = ref([])
const showCardModal = ref(false)
const showAchievementModal = ref(false)
const showImageModal = ref(false)
const originalImage = ref(null)

const replyTabs = [
  { id: 'gentle', icon: '🌸', name: '温柔版' },
  { id: 'humor', icon: '😄', name: '幽默版' },
  { id: 'direct', icon: '💪', name: '直接版' },
  { id: 'smart', icon: '🧠', name: '高情商' },
  { id: 'flirty', icon: '😏', name: '撩人版' }
]

function getEmotionClass(score) {
  if (score >= 0.7) return 'positive'
  if (score >= 0.4) return 'neutral'
  return 'negative'
}

function getEmotionLabel(score) {
  if (score >= 0.8) return '很开心'
  if (score >= 0.6) return '开心'
  if (score >= 0.4) return '平静'
  if (score >= 0.2) return '低落'
  return '很沮丧'
}

function getRarityName(rarity) {
  const names = { 1: '普通', 2: '稀有', 3: '史诗', 4: '传说' }
  return names[rarity] || '普通'
}

function closeCardModal() {
  showCardModal.value = false
}

function closeAchievementModal() {
  showAchievementModal.value = false
}

function openImageModal() {
  showImageModal.value = true
}

function closeImageModal() {
  showImageModal.value = false
}

function downloadImage() {
  if (!originalImage.value) return
  
  const link = document.createElement('a')
  link.download = 'decoded-image.jpg'
  link.href = originalImage.value
  link.click()
  ElMessage.success('图片已下载')
}

function generateReplies() {
  if (!analysis.value?.translations?.length) return []

  const translations = analysis.value.translations
  const relationship = analysis.value.relationship || '朋友'
  const mainSubtext = translations[0]?.subtext || ''
  const mainLiteral = translations[0]?.literal || ''
  const lastSubtext = translations[translations.length - 1]?.subtext || ''

  return {
    gentle: [
      {
        label: '体贴问候',
        emotion: '😊 温和',
        emotionClass: 'emotion-positive',
        text: `嗯嗯，我懂的~ ${mainSubtext.replace(/[？?。.!！]/g, '')}，放心吧，我心里有数的 😊`
      },
      {
        label: '认真回应',
        emotion: '🤗 暖心',
        emotionClass: 'emotion-positive',
        text: `谢谢你的关心呀 💕 ${lastSubtext}，有你在真好~`
      },
      {
        label: '轻松闲聊',
        emotion: '😌 放松',
        emotionClass: 'emotion-neutral',
        text: `哈哈，你说得对！${mainSubtext.replace(/[？?。.!！]/g, '')}，改天有空聊~`
      }
    ],
    humor: [
      {
        label: '搞笑回复',
        emotion: '😂 搞笑',
        emotionClass: 'emotion-positive',
        text: `哈哈哈哈哈！${mainLiteral.replace(/[？?。.!！]/g, '')}，你是在逗我吗笑死我了🤣`
      },
      {
        label: '自嘲风格',
        emotion: '😆 调皮',
        emotionClass: 'emotion-positive',
        text: `${mainSubtext.replace(/[？?。.!！]/g, '')}？好好好，你说的都对，我就是那个大冤种~😅`
      },
      {
        label: '网络梗',
        emotion: '🤣 幽默',
        emotionClass: 'emotion-positive',
        text: `啊这... ${mainSubtext.replace(/[？?。.!！]/g, '')}，属实是没想到啊😂`
      }
    ],
    direct: [
      {
        label: '直截了当',
        emotion: '💼 干脆',
        emotionClass: 'emotion-neutral',
        text: `${mainSubtext.replace(/[？?。.!！]/g, '')}，说白了就是${lastSubtext}，我明白了。`
      },
      {
        label: '开门见山',
        emotion: '🎯 明确',
        emotionClass: 'emotion-neutral',
        text: `行，我知道了。${mainSubtext.replace(/[？?。.!！]/g, '')}，直接说重点吧~`
      },
      {
        label: '务实回复',
        emotion: '📋 实际',
        emotionClass: 'emotion-neutral',
        text: `${mainLiteral}，所以你的意思是${lastSubtext}，对吗？`
      }
    ],
    smart: [
      {
        label: '高情商回复',
        emotion: '✨ 情商高',
        emotionClass: 'emotion-positive',
        text: `哎呀，你这一说我才反应过来~ ${mainSubtext.replace(/[？?。.!！]/g, '')}，果然还是你懂我 💫`
      },
      {
        label: '聪明圆滑',
        emotion: '🧩 机智',
        emotionClass: 'emotion-positive',
        text: `${lastSubtext}是吧？被你戳中了哈哈，不过我觉得吧... ${mainSubtext.replace(/[？?。.!！]/g, '')}~`
      },
      {
        label: '化解尴尬',
        emotion: '😎 从容',
        emotionClass: 'emotion-positive',
        text: `哈，我懂你意思了！${mainSubtext.replace(/[？?。.!！]/g, '')}，不愧是咱俩，心有灵犀~`
      }
    ],
    flirty: [
      {
        label: '暧昧试探',
        emotion: '😏 暧昧',
        emotionClass: 'emotion-positive',
        text: `${mainSubtext.replace(/[？?。.!！]/g, '')}~你是不是在暗示我什么呀？😏`
      },
      {
        label: '撩人回复',
        emotion: '💫 撩人',
        emotionClass: 'emotion-positive',
        text: `被你发现了~ ${lastSubtext}，你是不是一直在关注我呀？😊`
      },
      {
        label: '调情风格',
        emotion: '🥰 甜蜜',
        emotionClass: 'emotion-positive',
        text: `哎呀~ ${mainSubtext.replace(/[？?。.!！]/g, '')}，就你会说话，说得我都不好意思了~ 💕`
      }
    ]
  }
}

const currentReplies = computed(() => {
  const replies = generateReplies()
  return replies[activeTab.value] || []
})

async function copyReply(reply) {
  try {
    await navigator.clipboard.writeText(reply.text)
    const index = currentReplies.value.indexOf(reply)
    copiedIndex.value = index
    ElMessage.success('已复制到剪贴板')
    setTimeout(() => {
      copiedIndex.value = -1
    }, 2000)
  } catch (e) {
    const textarea = document.createElement('textarea')
    textarea.value = reply.text
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    const index = currentReplies.value.indexOf(reply)
    copiedIndex.value = index
    ElMessage.success('已复制到剪贴板')
    setTimeout(() => {
      copiedIndex.value = -1
    }, 2000)
  }
}

function initChart() {
  if (!chartRef.value || !analysis.value?.emotionCurve) return

  const chart = echarts.init(chartRef.value)
  const data = analysis.value.emotionCurve

  const option = {
    grid: {
      left: 40,
      right: 20,
      top: 20,
      bottom: 30
    },
    xAxis: {
      type: 'category',
      data: data.map((d, i) => `${d.speaker}${i + 1}`),
      axisLine: { lineStyle: { color: 'rgba(212, 175, 55, 0.3)' } },
      axisLabel: { color: '#8B8B8B' }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 1,
      axisLine: { show: false },
      axisLabel: { color: '#8B8B8B' },
      splitLine: { lineStyle: { color: 'rgba(212, 175, 55, 0.1)' } }
    },
    series: [{
      type: 'line',
      smooth: true,
      data: data.map(d => d.emotionScore),
      lineStyle: {
        color: '#D4AF37',
        width: 3
      },
      itemStyle: {
        color: '#D4AF37'
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(212, 175, 55, 0.4)' },
          { offset: 1, color: 'rgba(212, 175, 55, 0)' }
        ])
      },
      label: {
        show: true,
        position: 'top',
        formatter: '{c}',
        color: '#D4AF37',
        fontSize: 10
      }
    }]
  }

  chart.setOption(option)
}

async function generateShareImage() {
  if (!resultContainer.value) return

  try {
    const canvas = await html2canvas(resultContainer.value, {
      backgroundColor: '#0B0E14',
      scale: 2
    })

    const link = document.createElement('a')
    link.download = '言外解码结果.png'
    link.href = canvas.toDataURL('image/png')
    link.click()

    await analysisApi.share()
  } catch (e) {
    console.error('Share failed:', e)
  }
}

function fireConfetti() {
  const myConfetti = confetti.create(confettiCanvas.value, {
    resize: true,
    useWorker: true
  })

  myConfetti({
    particleCount: 100,
    spread: 70,
    origin: { y: 0.6 },
    colors: ['#D4AF37', '#F5D061', '#B8860B']
  })
}

function goHome() {
  router.replace('/')
}

onMounted(() => {
  if (analysisStore.result) {
    const data = analysisStore.consumeResult()
    analysis.value = data.result
    newCard.value = data.newCard
    newAchievements.value = data.newAchievements || []
    const img = data.originalImage
    originalImage.value = img && !img.startsWith('data:') ? 'data:image/jpeg;base64,' + img : img

    nextTick(() => {
      initChart()

      if (newCard.value) {
        setTimeout(() => {
          showCardModal.value = true
          fireConfetti()
        }, 500)
      } else if (newAchievements.value.length > 0) {
        setTimeout(() => {
          showAchievementModal.value = true
          fireConfetti()
        }, 500)
      }
    })
  } else {
    router.replace('/')
  }
})
</script>

<style scoped>
.result-view {
  padding: 16px 16px 100px;
  position: relative;
}

.header {
  text-align: center;
  padding: 24px 0 28px;
  position: relative;
}

.back-btn {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
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
  z-index: 2;
}

.back-btn:hover {
  background: rgba(30, 41, 59, 0.8);
  color: #D4AF37;
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
  font-size: 32px;
  font-weight: 800;
  letter-spacing: 1px;
  margin-bottom: 6px;
  text-shadow: 0 0 30px rgba(212, 175, 55, 0.2);
}

.card {
  background: var(--bg-card);
  border-radius: 20px;
  border: 1px solid rgba(212, 175, 55, 0.15);
  padding: 20px;
  margin-bottom: 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.card:hover {
  border-color: rgba(212, 175, 55, 0.3);
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
}

.section-label {
  color: #94A3B8;
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 12px;
  letter-spacing: 0.5px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #D4AF37;
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 16px;
  letter-spacing: 0.5px;
}

.title-icon {
  font-size: 18px;
}

.image-section {
  text-align: center;
  padding: 16px;
}

.view-image-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: rgba(212, 175, 55, 0.1);
  border: 1px solid rgba(212, 175, 55, 0.3);
  border-radius: 12px;
  color: #D4AF37;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.view-image-btn:hover {
  background: rgba(212, 175, 55, 0.2);
  border-color: #D4AF37;
  transform: scale(1.02);
}

.btn-icon {
  font-size: 16px;
}

.relationship-section {
  text-align: center;
}

.relationship-tag {
  display: inline-block;
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.2), rgba(212, 175, 55, 0.1));
  border: 1px solid rgba(212, 175, 55, 0.3);
  padding: 12px 32px;
  border-radius: 24px;
  color: #D4AF37;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 1px;
}

.emotion-section .chart-container {
  height: 200px;
  width: 100%;
}

.translations-section {
  margin-bottom: 16px;
}

.translation-card {
  margin-bottom: 12px;
}

.original-text {
  background: rgba(212, 175, 55, 0.1);
  padding: 12px 16px;
  border-radius: 12px;
  margin-bottom: 16px;
  color: #F8FAFC;
  font-size: 15px;
  font-weight: 500;
  line-height: 1.6;
}

.translation-content {
  margin-bottom: 16px;
}

.translation-row {
  display: flex;
  margin-bottom: 8px;
  font-size: 14px;
  line-height: 1.6;
}

.translation-row .label {
  color: #94A3B8;
  min-width: 80px;
  font-weight: 500;
}

.translation-row .value {
  color: #F8FAFC;
  flex: 1;
}

.translation-row.subtext .value {
  color: #D4AF37;
  font-weight: 500;
}

.emotion-indicator {
  display: flex;
  align-items: center;
  gap: 12px;
}

.emotion-bar {
  flex: 1;
  height: 8px;
  background: #1E293B;
  border-radius: 4px;
  overflow: hidden;
}

.emotion-fill {
  height: 100%;
  transition: width 0.5s ease;
  border-radius: 4px;
}

.emotion-fill.positive {
  background: linear-gradient(90deg, #22c55e, #86efac);
}

.emotion-fill.neutral {
  background: linear-gradient(90deg, #eab308, #fde047);
}

.emotion-fill.negative {
  background: linear-gradient(90deg, #ef4444, #f87171);
}

.emotion-value {
  font-size: 12px;
  color: #94A3B8;
  min-width: 50px;
  font-weight: 500;
}

.advice-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.advice-list li {
  padding: 12px 0;
  border-bottom: 1px solid rgba(212, 175, 55, 0.1);
  color: #F8FAFC;
  font-size: 14px;
  line-height: 1.7;
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.advice-list li::before {
  content: '💡';
  flex-shrink: 0;
  font-size: 16px;
}

.advice-list li:last-child {
  border-bottom: none;
}

.reply-section {
  padding: 0;
  overflow: hidden;
}

.section-header {
  padding: 20px 20px 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-subtitle {
  font-size: 12px;
  color: #64748B;
}

.reply-tabs {
  display: flex;
  gap: 8px;
  padding: 16px 20px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

.reply-tabs::-webkit-scrollbar {
  display: none;
}

.reply-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  background: rgba(30, 41, 59, 0.5);
  border: 1px solid rgba(212, 175, 55, 0.1);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  flex-shrink: 0;
  font-size: 13px;
}

.reply-tab:hover {
  background: rgba(30, 41, 59, 0.7);
  border-color: rgba(212, 175, 55, 0.3);
}

.reply-tab.active {
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.2), rgba(212, 175, 55, 0.1));
  border-color: #D4AF37;
}

.tab-icon {
  font-size: 14px;
}

.tab-name {
  color: #F1F5F9;
  font-weight: 500;
}

.reply-list {
  padding: 0 20px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.reply-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: rgba(30, 41, 59, 0.5);
  border: 1px solid rgba(212, 175, 55, 0.1);
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.reply-item:hover {
  background: rgba(30, 41, 59, 0.7);
  border-color: rgba(212, 175, 55, 0.3);
  transform: translateX(4px);
}

.reply-item:active {
  transform: scale(0.98);
}

.reply-content {
  flex: 1;
  min-width: 0;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.reply-label {
  font-size: 12px;
  color: #94A3B8;
  font-weight: 500;
}

.reply-emotion {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 8px;
  background: rgba(212, 175, 55, 0.1);
}

.emotion-positive {
  color: #67c23a;
}

.emotion-neutral {
  color: #eab308;
}

.emotion-negative {
  color: #ef4444;
}

.reply-text {
  font-size: 14px;
  color: #F8FAFC;
  line-height: 1.6;
  word-break: break-all;
}

.copy-btn {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(212, 175, 55, 0.1);
  border-radius: 10px;
  transition: all 0.3s ease;
}

.reply-item:hover .copy-btn {
  background: rgba(212, 175, 55, 0.2);
}

.copy-icon {
  font-size: 16px;
}

.share-btn {
  width: 100%;
  padding: 16px 28px;
  font-size: 17px;
  border-radius: 16px;
  margin-top: 8px;
  font-weight: 600;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  backdrop-filter: blur(8px);
  padding: 20px;
}

.card-modal,
.achievement-modal {
  background: linear-gradient(145deg, rgba(30, 41, 59, 0.98), rgba(18, 24, 38, 0.98));
  border-radius: 24px;
  padding: 32px 24px;
  text-align: center;
  max-width: 320px;
  width: 100%;
  border: 2px solid;
  position: relative;
  overflow: hidden;
}

.card-modal::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 50% 0%, rgba(212, 175, 55, 0.1) 0%, transparent 60%);
  pointer-events: none;
}

.card-modal.rarity-1 {
  border-color: #64748b;
  box-shadow: 0 0 30px rgba(100, 116, 139, 0.2);
}

.card-modal.rarity-2 {
  border-color: #3b82f6;
  box-shadow: 0 0 40px rgba(59, 130, 246, 0.3);
}

.card-modal.rarity-3 {
  border-color: #a855f7;
  box-shadow: 0 0 50px rgba(168, 85, 247, 0.4);
}

.card-modal.rarity-4 {
  border-color: #D4AF37;
  box-shadow: 0 0 60px rgba(212, 175, 55, 0.5);
  animation: legendGlow 2s ease-in-out infinite;
}

@keyframes legendGlow {
  0%, 100% {
    box-shadow: 0 0 40px rgba(212, 175, 55, 0.3);
  }
  50% {
    box-shadow: 0 0 80px rgba(212, 175, 55, 0.7);
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 0.4;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.2);
  }
}

.card-icon {
  font-size: 80px;
  margin-bottom: 16px;
  display: block;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.card-name {
  font-size: 24px;
  font-weight: 800;
  color: #F8FAFC;
  margin-bottom: 8px;
  letter-spacing: 0.5px;
}

.card-rarity {
  font-size: 14px;
  color: #D4AF37;
  margin-bottom: 16px;
  font-weight: 600;
  letter-spacing: 1px;
}

.card-description {
  font-size: 14px;
  color: #94A3B8;
  margin-bottom: 24px;
  line-height: 1.7;
}

.close-btn {
  width: 100%;
  padding: 14px 24px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 14px;
}

.achievement-modal {
  border-color: #D4AF37;
  box-shadow: 0 0 50px rgba(212, 175, 55, 0.3);
}

.achievement-modal::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 50% 0%, rgba(212, 175, 55, 0.1) 0%, transparent 60%);
  pointer-events: none;
}

.modal-title {
  font-size: 22px;
  font-weight: 800;
  color: #D4AF37;
  margin-bottom: 24px;
  letter-spacing: 0.5px;
}

.achievement-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: rgba(212, 175, 55, 0.1);
  border-radius: 16px;
  margin-bottom: 12px;
  border: 1px solid rgba(212, 175, 55, 0.15);
}

.achievement-item:last-child {
  margin-bottom: 24px;
}

.achievement-icon {
  font-size: 40px;
  flex-shrink: 0;
}

.achievement-name {
  font-size: 15px;
  font-weight: 700;
  color: #F8FAFC;
  margin-bottom: 4px;
}

.achievement-desc {
  font-size: 13px;
  color: #94A3B8;
  line-height: 1.5;
}

.confetti-canvas {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 9998;
}

.image-modal {
  background: linear-gradient(145deg, rgba(30, 41, 59, 0.98), rgba(18, 24, 38, 0.98));
  border-radius: 24px;
  padding: 0;
  max-width: 90vw;
  max-height: 90vh;
  width: 100%;
  border: 1px solid rgba(212, 175, 55, 0.2);
  position: relative;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid rgba(212, 175, 55, 0.1);
}

.modal-header .modal-title {
  font-size: 18px;
  font-weight: 600;
  color: #F8FAFC;
}

.close-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(212, 175, 55, 0.1);
  border: none;
  border-radius: 8px;
  color: #94A3B8;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.close-icon:hover {
  background: rgba(239, 68, 68, 0.2);
  color: #EF4444;
}

.image-container {
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  max-height: 60vh;
  overflow: auto;
}

.preview-image {
  max-width: 100%;
  max-height: 50vh;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
}

.download-btn {
  width: calc(100% - 40px);
  margin: 0 20px 20px;
  padding: 14px 24px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}</style>