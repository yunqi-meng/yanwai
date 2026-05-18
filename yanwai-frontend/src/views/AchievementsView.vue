<template>
  <div class="achievements-view page-container">
    <div class="header">
      <div class="header-decoration">
        <div class="decoration-dot"></div>
        <div class="decoration-dot"></div>
        <div class="decoration-dot"></div>
      </div>
      <h1 class="title gold-gradient">成就墙</h1>
    </div>

    <div class="progress-section card">
      <div class="progress-info">
        <span class="progress-text">{{ unlockedCount }} / {{ totalCount }}</span>
        <span class="progress-label">成就达成</span>
      </div>
      <div class="progress-bar">
        <div
          class="progress-fill"
          :style="{ width: progressPercent + '%' }"
        ></div>
      </div>
    </div>

    <div class="achievements-grid">
      <div
        v-for="achievement in achievements"
        :key="achievement.id"
        class="achievement-card"
        :class="{ locked: !achievement.unlocked }"
        @click="showDetail(achievement)"
      >
        <div class="achievement-badge" :class="{ unlocked: achievement.unlocked }">
          {{ achievement.icon }}
        </div>
        <div class="achievement-name">{{ achievement.name }}</div>
        <div class="achievement-status">
          {{ achievement.unlocked ? '已解锁' : '未解锁' }}
        </div>
      </div>
    </div>

    <el-dialog
      v-model="showDialog"
      :title="currentAchievement?.name"
      width="320px"
      class="achievement-dialog"
    >
      <div class="dialog-content">
        <div class="dialog-icon" :class="{ unlocked: currentAchievement?.unlocked }">
          {{ currentAchievement?.icon }}
        </div>
        <div class="dialog-description">{{ currentAchievement?.description }}</div>
        <div class="dialog-reward" v-if="currentAchievement?.unlocked">
          <span class="reward-label">奖励：</span>
          <span class="reward-value">{{ currentAchievement?.rewardValue }}</span>
        </div>
        <div class="dialog-condition" v-if="!currentAchievement?.unlocked">
          <span class="condition-label">解锁条件：</span>
          <span class="condition-value">
            {{ getConditionText(currentAchievement) }}
          </span>
        </div>
        <div class="dialog-time" v-if="currentAchievement?.unlocked && currentAchievement?.unlockedAt">
          解锁时间：{{ formatDate(currentAchievement.unlockedAt) }}
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { achievementsApi } from '../api'

const achievements = ref([])
const showDialog = ref(false)
const currentAchievement = ref(null)

const unlockedCount = computed(() => {
  return achievements.value.filter(a => a.unlocked).length
})

const totalCount = computed(() => {
  return achievements.value.length
})

const progressPercent = computed(() => {
  if (totalCount.value === 0) return 0
  return Math.round((unlockedCount.value / totalCount.value) * 100)
})

function getConditionText(achievement) {
  if (!achievement) return ''
  const fieldNames = {
    total_analysis: '分析次数',
    total_share: '分享次数',
    late_night_count: '深夜分析次数',
    workplace_count: '职场分析次数',
    romance_count: '情感分析次数'
  }
  const fieldName = fieldNames[achievement.conditionField] || achievement.conditionField
  return `累计${fieldName}达到 ${achievement.conditionValue} 次`
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

function showDetail(achievement) {
  currentAchievement.value = achievement
  showDialog.value = true
}

async function fetchAchievements() {
  try {
    const res = await achievementsApi.getAll()
    achievements.value = res.data.data
  } catch (e) {
    console.error('Failed to fetch achievements:', e)
  }
}

onMounted(() => {
  fetchAchievements()
})
</script>

<style scoped>
.achievements-view {
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

.title {
  font-size: 32px;
  font-weight: 800;
  letter-spacing: 1px;
  margin-bottom: 6px;
  text-shadow: 0 0 30px rgba(212, 175, 55, 0.2);
}

.progress-section {
  text-align: center;
  margin-bottom: 28px;
  background: linear-gradient(145deg, rgba(30, 41, 59, 0.95), rgba(18, 24, 38, 0.95));
  border-radius: 20px;
  padding: 24px;
  border: 1px solid rgba(212, 175, 55, 0.15);
}

.progress-info {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 10px;
  margin-bottom: 16px;
}

.progress-text {
  font-size: 40px;
  font-weight: 800;
  color: #D4AF37;
  letter-spacing: 1px;
}

.progress-label {
  font-size: 14px;
  color: #94A3B8;
  font-weight: 500;
}

.progress-bar {
  height: 10px;
  background: #1E293B;
  border-radius: 5px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #B8860B, #D4AF37, #F5D061);
  transition: width 0.5s ease;
  border-radius: 5px;
}

.achievements-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.achievement-card {
  background: linear-gradient(145deg, rgba(30, 41, 59, 0.95), rgba(18, 24, 38, 0.95));
  border-radius: 20px;
  padding: 20px 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid transparent;
  position: relative;
  overflow: hidden;
}

.achievement-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 50% 0%, rgba(212, 175, 55, 0.08) 0%, transparent 60%);
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.3s;
}

.achievement-card:hover::before {
  opacity: 1;
}

.achievement-card:hover {
  border-color: rgba(212, 175, 55, 0.3);
  transform: translateY(-4px) scale(1.02);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.4);
}

.achievement-card.locked {
  opacity: 0.5;
  filter: grayscale(0.5);
}

.achievement-badge {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  margin: 0 auto 14px;
  background: linear-gradient(145deg, rgba(30, 41, 59, 0.9), rgba(18, 24, 38, 0.95));
  border: 3px solid rgba(100, 116, 139, 0.4);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.achievement-badge.unlocked {
  background: linear-gradient(135deg, rgba(184, 134, 11, 0.2), rgba(212, 175, 55, 0.2));
  border-color: #D4AF37;
  box-shadow: 0 0 30px rgba(212, 175, 55, 0.4);
  animation: badge-glow 2s ease-in-out infinite;
}

@keyframes badge-glow {
  0%, 100% {
    box-shadow: 0 0 20px rgba(212, 175, 55, 0.3);
  }
  50% {
    box-shadow: 0 0 40px rgba(212, 175, 55, 0.5);
  }
}

.achievement-name {
  font-size: 14px;
  font-weight: 700;
  color: #F8FAFC;
  margin-bottom: 6px;
  letter-spacing: 0.3px;
}

.achievement-status {
  font-size: 12px;
  color: #94A3B8;
  font-weight: 500;
}

.dialog-content {
  text-align: center;
}

.dialog-icon {
  font-size: 80px;
  margin-bottom: 20px;
  width: 110px;
  height: 110px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  background: linear-gradient(145deg, rgba(30, 41, 59, 0.9), rgba(18, 24, 38, 0.95));
  border: 3px solid rgba(100, 116, 139, 0.4);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.dialog-icon.unlocked {
  background: linear-gradient(135deg, rgba(184, 134, 11, 0.2), rgba(212, 175, 55, 0.2));
  border-color: #D4AF37;
  box-shadow: 0 0 40px rgba(212, 175, 55, 0.4);
}

.dialog-description {
  font-size: 14px;
  color: #94A3B8;
  margin-bottom: 20px;
  line-height: 1.7;
}

.dialog-reward,
.dialog-condition,
.dialog-time {
  font-size: 14px;
  margin-bottom: 10px;
  padding: 12px 16px;
  background: rgba(30, 41, 59, 0.6);
  border-radius: 12px;
  border: 1px solid rgba(212, 175, 55, 0.1);
}

.dialog-reward:last-child,
.dialog-condition:last-child,
.dialog-time:last-child {
  margin-bottom: 0;
}

.reward-label,
.condition-label {
  color: #94A3B8;
  font-weight: 500;
}

.reward-value,
.condition-value {
  color: #D4AF37;
  font-weight: 600;
}

.dialog-time {
  color: #64748B;
  font-size: 12px;
}

.achievement-dialog :deep(.el-dialog) {
  background: linear-gradient(145deg, rgba(30, 41, 59, 0.98), rgba(18, 24, 38, 0.98));
  border-radius: 20px;
  border: 1px solid rgba(212, 175, 55, 0.2);
}

.achievement-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid rgba(212, 175, 55, 0.15);
  padding: 20px 24px;
}

.achievement-dialog :deep(.el-dialog__title) {
  color: #D4AF37;
  font-weight: 700;
  font-size: 18px;
  letter-spacing: 0.5px;
}

.achievement-dialog :deep(.el-dialog__body) {
  padding: 24px;
}
</style>
