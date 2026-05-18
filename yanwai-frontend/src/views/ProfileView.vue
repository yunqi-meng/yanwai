<template>
  <div class="profile-view page-container">
    <div class="header">
      <div class="header-decoration">
        <div class="decoration-dot"></div>
        <div class="decoration-dot"></div>
        <div class="decoration-dot"></div>
      </div>
      <h1 class="title gold-gradient">我的</h1>
    </div>

    <div class="profile-card card">
      <div class="avatar-section">
        <div class="avatar">
          {{ userStore.nickname?.charAt(0) || '?' }}
        </div>
        <div class="user-info">
          <div class="nickname">{{ userStore.nickname || '加载中...' }}</div>
          <div class="member-badge" :class="{ vip: userStore.memberLevel === 1 }">
            {{ userStore.memberLevel === 1 ? '⭐ 会员' : '免费用户' }}
          </div>
          <div class="member-expire" v-if="userStore.memberLevel === 1 && userStore.memberExpireTime">
            到期时间：{{ userStore.memberExpireTime }}
          </div>
        </div>
      </div>
    </div>

    <div class="stats-section card">
      <div class="section-title">我的数据</div>
      <div class="stats-grid">
        <div class="stat-item">
          <div class="stat-value">{{ userStore.totalAnalysis }}</div>
          <div class="stat-label">总解码</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ userStore.totalShare }}</div>
          <div class="stat-label">分享次数</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ userStore.remainingAnalysis }}</div>
          <div class="stat-label">今日剩余</div>
        </div>
      </div>
    </div>

    <div class="stats-section card">
      <div class="section-title">行为统计</div>
      <div class="behavior-stats">
        <div class="behavior-item">
          <span class="behavior-icon">🦉</span>
          <span class="behavior-label">深夜解码</span>
          <span class="behavior-value">{{ userStore.lateNightCount }}次</span>
        </div>
        <div class="behavior-item">
          <span class="behavior-icon">💼</span>
          <span class="behavior-label">职场分析</span>
          <span class="behavior-value">{{ userStore.workplaceCount }}次</span>
        </div>
        <div class="behavior-item">
          <span class="behavior-icon">💕</span>
          <span class="behavior-label">情感分析</span>
          <span class="behavior-value">{{ userStore.romanceCount }}次</span>
        </div>
      </div>
    </div>

    <div class="vip-section card" v-if="userStore.memberLevel !== 1">
      <div class="vip-banner">
        <div class="vip-content">
          <div class="vip-title">开通会员</div>
          <div class="vip-benefits">
            <div class="benefit-item">✓ 每日10次解码</div>
            <div class="benefit-item">✓ 传说卡牌概率翻倍</div>
            <div class="benefit-item">✓ 专属会员标识</div>
          </div>
        </div>
        <button class="vip-btn" @click="openAdDialog" :disabled="!userStore.canWatchAd">
          {{ userStore.canWatchAd ? '观看广告开通' : '今日已观看' }}
        </button>
      </div>
    </div>

    <div class="vip-section card" v-else>
      <div class="vip-active-banner">
        <div class="vip-icon">⭐</div>
        <div class="vip-info">
          <div class="vip-status">会员已开通</div>
          <div class="vip-expire-time" v-if="userStore.memberExpireTime">
            到期时间：{{ userStore.memberExpireTime }}
          </div>
        </div>
      </div>
    </div>

    <div class="actions-section card">
      <div class="action-item" @click="showAbout = true">
        <span class="action-icon">ℹ️</span>
        <span class="action-label">关于言外</span>
        <span class="action-arrow">›</span>
      </div>
      <div class="action-item danger" @click="confirmReset">
        <span class="action-icon">🗑️</span>
        <span class="action-label">重置数据</span>
        <span class="action-arrow">›</span>
      </div>
    </div>

    <el-dialog
      v-model="showAbout"
      title="关于言外"
      width="320px"
      class="about-dialog"
    >
      <div class="about-content">
        <div class="app-icon">🔮</div>
        <div class="app-name">言外</div>
        <div class="app-subtitle">听懂TA的潜台词</div>
        <div class="app-version">版本 1.0.0</div>
        <div class="app-description">
          基于AI深度分析的对话潜台词解读工具，附带游戏化的人格卡牌收集系统。
        </div>
        <div class="copyright">
          © 2024 言外 All Rights Reserved
        </div>
      </div>
    </el-dialog>

    <el-dialog
      v-model="showAdDialog"
      title="观看广告"
      width="400px"
      class="ad-dialog"
      :close-on-click-modal="false"
      :show-close="false"
    >
      <div class="ad-container">
        <video
          ref="videoRef"
          :src="adVideoUrl"
          controls
          autoplay
          @ended="onAdEnded"
          class="ad-video"
        >
          您的浏览器不支持视频播放
        </video>
        <div class="ad-hint" v-if="!adCompleted">
          请完整观看广告以开通会员
        </div>
        <div class="ad-success" v-else>
          ✓ 广告观看完成，会员已开通！
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useUserStore } from '../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const showAbout = ref(false)
const showAdDialog = ref(false)
const adCompleted = ref(false)
const videoRef = ref(null)
const adVideoUrl = '/advertisement.mp4'

function openAdDialog() {
  if (!userStore.canWatchAd) {
    ElMessage.warning('今日已观看过广告，请明天再来')
    return
  }
  showAdDialog.value = true
  adCompleted.value = false
  setTimeout(() => {
    if (videoRef.value) {
      videoRef.value.currentTime = 0
    }
  }, 100)
}

async function onAdEnded() {
  if (adCompleted.value) return
  adCompleted.value = true
  
  const success = await userStore.watchAdComplete()
  if (success) {
    ElMessage.success('会员开通成功！')
    showAdDialog.value = false
  } else {
    ElMessage.error('开通失败，请重试')
  }
}

watch(showAdDialog, (newVal) => {
  if (!newVal && videoRef.value) {
    videoRef.value.pause()
  }
})

async function confirmReset() {
  try {
    await ElMessageBox.confirm(
      '确定要重置所有数据吗？这将清除您的分析记录、分享次数等信息。',
      '确认重置',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await userStore.resetData()
    ElMessage.success('数据已重置')
  } catch {
  }
}

onMounted(async () => {
  await userStore.fetchStats()
})
</script>

<style scoped>
.member-expire {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

.vip-active-banner {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: linear-gradient(135deg, #ffd700, #ffb347);
  border-radius: 12px;
}

.vip-icon {
  font-size: 48px;
}

.vip-info {
  flex: 1;
}

.vip-status {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.vip-expire-time {
  font-size: 14px;
  color: #555;
  margin-top: 8px;
}

.ad-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.ad-video {
  width: 100%;
  max-height: 300px;
  border-radius: 8px;
  background: #000;
}

.ad-hint {
  font-size: 14px;
  color: #666;
  text-align: center;
}

.ad-success {
  font-size: 16px;
  color: #67c23a;
  text-align: center;
  font-weight: bold;
}
</style>
