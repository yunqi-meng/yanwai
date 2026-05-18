<template>
  <div class="result-view page-container" ref="resultContainer">
    <div class="header">
      <div class="header-decoration">
        <div class="decoration-dot"></div>
        <div class="decoration-dot"></div>
        <div class="decoration-dot"></div>
      </div>
      <h1 class="title gold-gradient">解码结果</h1>
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

    <canvas ref="confettiCanvas" class="confetti-canvas"></canvas>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import html2canvas from 'html2canvas'
import confetti from 'canvas-confetti'
import { useRouter } from 'vue-router'
import { analysisApi } from '../api'

const router = useRouter()
const resultContainer = ref(null)
const chartRef = ref(null)
const confettiCanvas = ref(null)

const analysis = ref(null)
const newCard = ref(null)
const newAchievements = ref([])
const showCardModal = ref(false)
const showAchievementModal = ref(false)

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

onMounted(() => {
  const stored = sessionStorage.getItem('decodeResult')
  if (stored) {
    const result = JSON.parse(stored)
    analysis.value = result.analysis
    newCard.value = result.newCard
    newAchievements.value = result.newAchievements || []

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
  font-size: 32px;
  font-weight: 800;
  letter-spacing: 1px;
  margin-bottom: 6px;
  text-shadow: 0 0 30px rgba(212, 175, 55, 0.2);
}

.card {
  background: linear-gradient(145deg, rgba(30, 41, 59, 0.95), rgba(18, 24, 38, 0.95));
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
  color: #D4AF37;
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 16px;
  letter-spacing: 0.5px;
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
</style>
