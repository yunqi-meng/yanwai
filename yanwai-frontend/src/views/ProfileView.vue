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
        <div class="avatar-wrapper" @click="openEditDialog">
          <div class="avatar" :class="{ vip: userStore.memberLevel === 1 }">
            {{ userStore.nickname?.charAt(0) || '?' }}
          </div>
          <div class="avatar-ring" v-if="userStore.memberLevel === 1"></div>
          <div class="vip-badge-icon" v-if="userStore.memberLevel === 1">⭐</div>
          <div class="edit-overlay">
            <span class="edit-icon">✏️</span>
          </div>
        </div>
        <div class="user-info">
          <div class="nickname-row">
            <div class="nickname">{{ userStore.nickname || '加载中...' }}</div>
            <div class="edit-btn" @click="openEditDialog">✏️</div>
          </div>
          <div class="member-badge" :class="{ vip: userStore.memberLevel === 1 }">
            <span class="badge-icon">{{ userStore.memberLevel === 1 ? '⭐' : '👤' }}</span>
            <span>{{ userStore.memberLevel === 1 ? '会员' : '免费用户' }}</span>
          </div>
          <div class="member-expire" v-if="userStore.memberLevel === 1 && userStore.memberExpireTime">
            <span class="expire-icon">⏰</span>
            到期时间：{{ userStore.memberExpireTime }}
          </div>
        </div>
      </div>
    </div>

    <div class="stats-section card">
      <div class="section-header">
        <div class="section-title">
          <span class="title-icon">📊</span>
          我的数据
        </div>
      </div>
      <div class="stats-grid">
        <div class="stat-item">
          <div class="stat-icon">🔍</div>
          <div class="stat-value">{{ userStore.totalAnalysis }}</div>
          <div class="stat-label">总解码</div>
        </div>
        <div class="stat-item">
          <div class="stat-icon">📤</div>
          <div class="stat-value">{{ userStore.totalShare }}</div>
          <div class="stat-label">分享次数</div>
        </div>
        <div class="stat-item">
          <div class="stat-icon">✨</div>
          <div class="stat-value highlight">{{ userStore.remainingAnalysis }}</div>
          <div class="stat-label">今日剩余</div>
        </div>
      </div>
    </div>

    <div class="history-section card">
      <div class="history-btn-wrapper">
        <button class="history-btn" @click="router.push('/history')">
          <span class="history-btn-icon">📝</span>
          <span class="history-btn-text">解码历史</span>
          <span class="history-btn-arrow">›</span>
        </button>
      </div>
    </div>

    <div class="theme-section card">
      <div class="section-header">
        <div class="section-title">
          <span class="title-icon">🎨</span>
          主题设置
        </div>
      </div>
      <div class="theme-toggle">
        <div class="theme-label">
          <span class="theme-icon">{{ themeStore.theme === 'dark' ? '🌙' : '☀️' }}</span>
          <span>{{ themeStore.theme === 'dark' ? '暗夜模式' : '白日模式' }}</span>
        </div>
        <button class="theme-switch" @click="themeStore.toggleTheme">
          <span class="switch-handle" :class="{ active: themeStore.theme === 'light' }"></span>
        </button>
      </div>
    </div>

    <div class="stats-section card">
      <div class="section-header">
        <div class="section-title">
          <span class="title-icon">📈</span>
          行为统计
        </div>
      </div>
      <div class="behavior-stats">
        <div class="behavior-item">
          <div class="behavior-icon-wrapper">🦉</div>
          <div class="behavior-info">
            <span class="behavior-label">深夜解码</span>
            <span class="behavior-value">{{ userStore.lateNightCount }}次</span>
          </div>
        </div>
        <div class="behavior-item">
          <div class="behavior-icon-wrapper">💼</div>
          <div class="behavior-info">
            <span class="behavior-label">职场分析</span>
            <span class="behavior-value">{{ userStore.workplaceCount }}次</span>
          </div>
        </div>
        <div class="behavior-item">
          <div class="behavior-icon-wrapper">💕</div>
          <div class="behavior-info">
            <span class="behavior-label">情感分析</span>
            <span class="behavior-value">{{ userStore.romanceCount }}次</span>
          </div>
        </div>
      </div>
    </div>

    <div class="vip-section card" v-if="userStore.memberLevel !== 1">
      <div class="vip-banner">
        <div class="vip-left">
          <div class="vip-icon-large">⭐</div>
        </div>
        <div class="vip-content">
          <div class="vip-title">开通会员</div>
          <div class="vip-benefits">
            <div class="benefit-item">
              <span class="check-icon">✓</span>
              <span>每日10次解码</span>
            </div>
            <div class="benefit-item">
              <span class="check-icon">✓</span>
              <span>传说卡牌概率翻倍</span>
            </div>
            <div class="benefit-item">
              <span class="check-icon">✓</span>
              <span>专属会员标识</span>
            </div>
          </div>
        </div>
        <button class="vip-btn" @click="openAdDialog" :disabled="!userStore.canWatchAd">
          {{ userStore.canWatchAd ? '观看广告开通' : '今日已观看' }}
        </button>
      </div>
    </div>

    <div class="vip-section card" v-else>
      <div class="vip-active-banner">
        <div class="vip-glow"></div>
        <div class="vip-icon-large">⭐</div>
        <div class="vip-info">
          <div class="vip-status">
            <span class="status-icon">👑</span>
            会员已开通
          </div>
          <div class="vip-expire-time" v-if="userStore.memberExpireTime">
            <span class="expire-icon">⏰</span>
            到期时间：{{ userStore.memberExpireTime }}
          </div>
        </div>
        <div class="vip-shimmer"></div>
      </div>
    </div>

    <div class="actions-section card">
      <div class="action-item" @click="showAbout = true">
        <div class="action-icon-wrapper">ℹ️</div>
        <span class="action-label">关于言外</span>
        <span class="action-arrow">›</span>
      </div>
      <div class="action-item danger" @click="confirmLogout">
        <div class="action-icon-wrapper">🚪</div>
        <span class="action-label">退出登录</span>
        <span class="action-arrow">›</span>
      </div>
      <div class="action-item danger" @click="confirmReset">
        <div class="action-icon-wrapper">🗑️</div>
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

    <el-dialog
      v-model="showEditDialog"
      title="修改资料"
      width="360px"
      class="edit-dialog"
    >
      <div class="edit-form">
        <div class="avatar-preview-section">
          <div class="avatar-preview">
            <div class="avatar" :class="{ vip: userStore.memberLevel === 1 }">
              {{ editNickname?.charAt(0) || '?' }}
            </div>
          </div>
          <div class="preview-hint">头像预览</div>
        </div>
        <div class="form-item">
          <label class="form-label">昵称</label>
          <el-input
            v-model="editNickname"
            placeholder="请输入昵称"
            maxlength="20"
            show-word-limit
            class="edit-input"
          />
        </div>
        <div class="avatar-tips">
          <div class="tips-title">💡 温馨提示</div>
          <div class="tips-item">• 昵称长度不超过20个字符</div>
          <div class="tips-item">• 头像会根据昵称首字自动生成</div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="showEditDialog = false">取消</button>
          <button class="btn-confirm" @click="saveNickname" :disabled="saving">
            {{ saving ? '保存中...' : '保存' }}
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { useThemeStore } from '../stores/theme'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()
const showAbout = ref(false)
const showAdDialog = ref(false)
const showEditDialog = ref(false)
const editNickname = ref('')
const adCompleted = ref(false)
const saving = ref(false)
const videoRef = ref(null)
const adVideoUrl = '/advertisement.mp4'

function openEditDialog() {
  editNickname.value = userStore.nickname || ''
  showEditDialog.value = true
}

async function saveNickname() {
  if (!editNickname.value.trim()) {
    ElMessage.warning('请输入昵称')
    return
  }
  saving.value = true
  try {
    const success = await userStore.updateUserInfo(editNickname.value.trim())
    if (success) {
      ElMessage.success('修改成功')
      showEditDialog.value = false
    } else {
      ElMessage.error('昵称已被使用，请更换其他昵称')
    }
  } finally {
    saving.value = false
  }
}

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

async function confirmLogout() {
  try {
    await ElMessageBox.confirm(
      '确定要退出登录吗？',
      '退出登录',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/auth')
  } catch {
  }
}

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
.profile-view {
  padding-bottom: 100px;
}

.history-section {
  padding: 0;
  overflow: hidden;
}

.history-btn-wrapper {
  border-bottom: 1px solid rgba(212, 175, 55, 0.1);
}

.history-btn {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  background: transparent;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
}

.history-btn:hover {
  background: rgba(212, 175, 55, 0.05);
}

.history-btn-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: rgba(30, 41, 59, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.history-btn-text {
  flex: 1;
  font-size: 15px;
  color: var(--text-primary);
  text-align: left;
}

.history-btn-arrow {
  font-size: 24px;
  color: #64748B;
  transition: all 0.3s ease;
}

.history-btn:hover .history-btn-arrow {
  transform: translateX(4px);
  color: #D4AF37;
}

.theme-section {
  padding: 20px;
}

.theme-toggle {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.theme-label {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  color: var(--text-primary);
}

.theme-icon {
  font-size: 24px;
}

.theme-switch {
  position: relative;
  width: 56px;
  height: 28px;
  border-radius: 14px;
  background: rgba(30, 41, 59, 0.8);
  border: 2px solid rgba(212, 175, 55, 0.3);
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 0;
  display: flex;
  align-items: center;
  transition: background 0.3s ease, border-color 0.3s ease;
}

body.theme-light .theme-switch {
  background: rgba(226, 232, 240, 0.8);
  border-color: rgba(212, 175, 55, 0.5);
}

.switch-handle {
  position: absolute;
  left: 2px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: linear-gradient(135deg, #64748B, #94A3B8);
  transition: transform 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.switch-handle.active {
  transform: translateX(28px);
  background: linear-gradient(135deg, #D4AF37, #F5D061);
  box-shadow: 0 2px 12px rgba(212, 175, 55, 0.5);
}

.header {
  text-align: center;
  padding: 16px 0 24px;
  position: relative;
}

.header-decoration {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-bottom: 12px;
}

.decoration-dot {
  width: 8px;
  height: 8px;
  background: linear-gradient(135deg, #D4AF37, #F5D061);
  border-radius: 50%;
  animation: dotPulse 2s ease-in-out infinite;
}

.decoration-dot:nth-child(2) {
  animation-delay: 0.3s;
}

.decoration-dot:nth-child(3) {
  animation-delay: 0.6s;
}

@keyframes dotPulse {
  0%, 100% { transform: scale(1); opacity: 0.6; }
  50% { transform: scale(1.3); opacity: 1; }
}

.title {
  font-size: 32px;
  font-weight: 700;
  margin: 0;
  letter-spacing: 2px;
}

.profile-card {
  padding: 24px;
  position: relative;
  overflow: hidden;
}

.profile-card::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -20%;
  width: 100px;
  height: 100px;
  background: radial-gradient(circle, rgba(212, 175, 55, 0.15) 0%, transparent 70%);
  border-radius: 50%;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
  position: relative;
  z-index: 1;
}

.avatar-wrapper {
  position: relative;
  cursor: pointer;
}

.edit-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.avatar-wrapper:hover .edit-overlay {
  opacity: 1;
}

.edit-icon {
  font-size: 24px;
}

.nickname-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.edit-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(212, 175, 55, 0.1);
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
  opacity: 0.6;
}

.edit-btn:hover {
  opacity: 1;
  background: rgba(212, 175, 55, 0.2);
  transform: scale(1.1);
}

.avatar {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  font-weight: 700;
  background: linear-gradient(135deg, #1E293B, #334155);
  border: 3px solid rgba(212, 175, 55, 0.3);
  transition: all 0.3s ease;
  position: relative;
}

body.theme-light .avatar {
  background: linear-gradient(135deg, #E2E8F0, #CBD5E1);
  border-color: rgba(212, 175, 55, 0.4);
}

.avatar.vip {
  background: linear-gradient(135deg, #B8860B, #D4AF37);
  color: #0B0E14;
  border-color: #F5D061;
  box-shadow: 0 0 30px rgba(212, 175, 55, 0.4);
}

.avatar-ring {
  position: absolute;
  top: -6px;
  left: -6px;
  right: -6px;
  bottom: -6px;
  border: 2px solid #D4AF37;
  border-radius: 50%;
  animation: ringRotate 8s linear infinite;
}

@keyframes ringRotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.vip-badge-icon {
  position: absolute;
  bottom: -4px;
  right: -4px;
  font-size: 20px;
  animation: badgeBounce 1.5s ease-in-out infinite;
}

@keyframes badgeBounce {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.15); }
}

.user-info {
  flex: 1;
}

.nickname {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.member-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 20px;
  background: rgba(148, 163, 184, 0.2);
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

body.theme-light .member-badge {
  background: rgba(100, 116, 139, 0.15);
}

.member-badge.vip {
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.3), rgba(212, 175, 55, 0.1));
  color: #D4AF37;
  box-shadow: 0 0 15px rgba(212, 175, 55, 0.3);
}

.badge-icon {
  font-size: 14px;
}

.member-expire {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #666;
}

.expire-icon {
  font-size: 12px;
}

.stats-section {
  padding: 20px;
}

.section-header {
  margin-bottom: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.title-icon {
  font-size: 18px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 12px;
  background: rgba(30, 41, 59, 0.5);
  border-radius: 16px;
  border: 1px solid rgba(212, 175, 55, 0.1);
  transition: all 0.3s ease;
}

body.theme-light .stat-item {
  background: rgba(226, 232, 240, 0.5);
  border-color: rgba(0, 0, 0, 0.05);
}

.stat-item:hover {
  transform: translateY(-3px);
  border-color: rgba(212, 175, 55, 0.3);
  background: rgba(30, 41, 59, 0.7);
}

body.theme-light .stat-item:hover {
  background: rgba(203, 213, 225, 0.7);
  border-color: rgba(212, 175, 55, 0.4);
}

.stat-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.stat-value.highlight {
  color: #D4AF37;
  text-shadow: 0 0 20px rgba(212, 175, 55, 0.5);
}

.stat-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.behavior-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.behavior-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px;
  background: rgba(30, 41, 59, 0.4);
  border-radius: 12px;
  border: 1px solid rgba(212, 175, 55, 0.08);
  transition: all 0.3s ease;
}

body.theme-light .behavior-item {
  background: rgba(226, 232, 240, 0.4);
  border-color: rgba(0, 0, 0, 0.05);
}

.behavior-item:hover {
  background: rgba(30, 41, 59, 0.6);
  border-color: rgba(212, 175, 55, 0.2);
}

body.theme-light .behavior-item:hover {
  background: rgba(203, 213, 225, 0.6);
  border-color: rgba(212, 175, 55, 0.3);
}

.behavior-icon-wrapper {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: rgba(212, 175, 55, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}

body.theme-light .behavior-icon-wrapper {
  background: rgba(212, 175, 55, 0.12);
}

.behavior-info {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.behavior-label {
  font-size: 14px;
  color: var(--text-primary);
}

.behavior-value {
  font-size: 14px;
  font-weight: 600;
  color: #D4AF37;
}

.vip-section {
  padding: 0;
  overflow: hidden;
}

.vip-banner {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: linear-gradient(135deg, rgba(184, 134, 11, 0.15), rgba(212, 175, 55, 0.08));
  border: 1px solid rgba(212, 175, 55, 0.2);
}

.vip-left {
  flex-shrink: 0;
}

.vip-icon-large {
  font-size: 48px;
  animation: vipGlow 2s ease-in-out infinite;
}

@keyframes vipGlow {
  0%, 100% { filter: drop-shadow(0 0 10px rgba(212, 175, 55, 0.5)); }
  50% { filter: drop-shadow(0 0 25px rgba(212, 175, 55, 0.8)); }
}

.vip-content {
  flex: 1;
}

.vip-title {
  font-size: 18px;
  font-weight: 700;
  color: #D4AF37;
  margin-bottom: 10px;
}

.vip-benefits {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.benefit-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-secondary);
}

.check-icon {
  color: #67c23a;
  font-size: 12px;
}

.vip-btn {
  background: linear-gradient(135deg, #B8860B, #D4AF37);
  border: none;
  color: #0B0E14;
  font-weight: 700;
  font-size: 14px;
  padding: 14px 22px;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  flex-shrink: 0;
}

.vip-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s;
}

.vip-btn:hover {
  background: linear-gradient(135deg, #D4AF37, #F5D061);
  box-shadow: 0 0 25px rgba(212, 175, 55, 0.5);
  transform: translateY(-2px);
}

.vip-btn:hover::before {
  left: 100%;
}

.vip-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
  background: #475569;
}

.vip-active-banner {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  background: linear-gradient(135deg, rgba(184, 134, 11, 0.25), rgba(212, 175, 55, 0.15));
  border: 1px solid rgba(212, 175, 55, 0.35);
  position: relative;
  overflow: hidden;
}

.vip-glow {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, rgba(212, 175, 55, 0.3) 0%, transparent 70%);
  animation: glowPulse 3s ease-in-out infinite;
}

@keyframes glowPulse {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.2); opacity: 0.8; }
}

.vip-shimmer {
  position: absolute;
  top: 0;
  left: -100%;
  width: 50%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  animation: shimmerMove 3s infinite;
}

@keyframes shimmerMove {
  0% { left: -100%; }
  100% { left: 200%; }
}

.vip-info {
  flex: 1;
  position: relative;
  z-index: 1;
}

.vip-status {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 22px;
  font-weight: 700;
  color: #D4AF37;
  margin-bottom: 8px;
}

.status-icon {
  font-size: 20px;
}

.vip-expire-time {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-secondary);
}

.actions-section {
  padding: 0;
  overflow: hidden;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  border-bottom: 1px solid rgba(212, 175, 55, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
}

body.theme-light .action-item {
  border-bottom-color: rgba(0, 0, 0, 0.05);
}

.action-item:last-child {
  border-bottom: none;
}

.action-item:hover {
  background: rgba(212, 175, 55, 0.05);
}

body.theme-light .action-item:hover {
  background: rgba(212, 175, 55, 0.08);
}

.action-item.danger:hover {
  background: rgba(239, 68, 68, 0.1);
}

.action-item.danger .action-label {
  color: #EF4444;
}

.action-icon-wrapper {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: rgba(30, 41, 59, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

body.theme-light .action-icon-wrapper {
  background: rgba(226, 232, 240, 0.8);
}

.action-label {
  flex: 1;
  font-size: 15px;
  color: var(--text-primary);
}

.action-arrow {
  font-size: 24px;
  color: var(--text-tertiary);
  transition: all 0.3s ease;
}

.action-item:hover .action-arrow {
  transform: translateX(4px);
  color: #D4AF37;
}

.about-dialog :deep(.el-dialog) {
  background: rgba(18, 24, 38, 0.98);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 20px;
}

body.theme-light .about-dialog :deep(.el-dialog) {
  background: rgba(255, 255, 255, 0.98);
  border-color: rgba(0, 0, 0, 0.08);
}

.about-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid rgba(212, 175, 55, 0.1);
  padding: 20px 24px;
}

body.theme-light .about-dialog :deep(.el-dialog__header) {
  border-bottom-color: rgba(0, 0, 0, 0.05);
}

.about-dialog :deep(.el-dialog__title) {
  color: var(--text-primary);
  font-size: 18px;
  font-weight: 600;
}

.about-content {
  text-align: center;
  padding: 24px 0;
}

.app-icon {
  font-size: 64px;
  margin-bottom: 16px;
  animation: floatIcon 3s ease-in-out infinite;
}

@keyframes floatIcon {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.app-name {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.app-subtitle {
  font-size: 14px;
  color: #D4AF37;
  margin-bottom: 16px;
}

.app-version {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-bottom: 16px;
}

.app-description {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
  padding: 0 16px;
  margin-bottom: 20px;
}

.copyright {
  font-size: 11px;
  color: var(--text-tertiary);
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
  border-radius: 12px;
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

.ad-dialog :deep(.el-dialog) {
  background: rgba(18, 24, 38, 0.98);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 20px;
}

body.theme-light .ad-dialog :deep(.el-dialog) {
  background: rgba(255, 255, 255, 0.98);
  border-color: rgba(0, 0, 0, 0.08);
}

.edit-dialog :deep(.el-dialog) {
  background: rgba(18, 24, 38, 0.98);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 20px;
}

body.theme-light .edit-dialog :deep(.el-dialog) {
  background: rgba(255, 255, 255, 0.98);
  border-color: rgba(0, 0, 0, 0.08);
}

.edit-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid rgba(212, 175, 55, 0.1);
  padding: 20px 24px;
}

body.theme-light .edit-dialog :deep(.el-dialog__header) {
  border-bottom-color: rgba(0, 0, 0, 0.05);
}

.edit-dialog :deep(.el-dialog__title) {
  color: var(--text-primary);
  font-size: 18px;
  font-weight: 600;
}

.edit-dialog :deep(.el-dialog__body) {
  padding: 24px;
}

.edit-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.avatar-preview-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.avatar-preview .avatar {
  width: 80px;
  height: 80px;
  font-size: 32px;
}

.preview-hint {
  font-size: 12px;
  color: var(--text-tertiary);
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 500;
}

.edit-input :deep(.el-input__wrapper) {
  background: rgba(30, 41, 59, 0.8) !important;
  border: 1px solid rgba(212, 175, 55, 0.2) !important;
  border-radius: 12px !important;
  box-shadow: none !important;
  padding: 12px 16px !important;
}

body.theme-light .edit-input :deep(.el-input__wrapper) {
  background: rgba(226, 232, 240, 0.8) !important;
  border-color: rgba(0, 0, 0, 0.1) !important;
}

.edit-input :deep(.el-input__inner) {
  color: var(--text-primary) !important;
  font-size: 15px !important;
}

.edit-input :deep(.el-input__inner::placeholder) {
  color: var(--text-tertiary) !important;
}

.edit-input :deep(.el-input__count) {
  color: var(--text-tertiary) !important;
  font-size: 11px !important;
}

.avatar-tips {
  background: rgba(212, 175, 55, 0.05);
  border: 1px solid rgba(212, 175, 55, 0.1);
  border-radius: 12px;
  padding: 14px;
}

body.theme-light .avatar-tips {
  background: rgba(212, 175, 55, 0.08);
  border-color: rgba(212, 175, 55, 0.15);
}

.tips-title {
  font-size: 13px;
  color: #D4AF37;
  margin-bottom: 8px;
  font-weight: 600;
}

.tips-item {
  font-size: 12px;
  color: #94A3B8;
  line-height: 1.6;
}

.dialog-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.btn-cancel {
  flex: 1;
  padding: 12px 20px;
  background: rgba(30, 41, 59, 0.8);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 12px;
  color: #94A3B8;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-cancel:hover {
  background: rgba(30, 41, 59, 1);
  border-color: rgba(212, 175, 55, 0.4);
  color: #F8FAFC;
}

.btn-confirm {
  flex: 1;
  padding: 12px 20px;
  background: linear-gradient(135deg, #B8860B, #D4AF37);
  border: none;
  border-radius: 12px;
  color: #0B0E14;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-confirm:hover {
  background: linear-gradient(135deg, #D4AF37, #F5D061);
  box-shadow: 0 0 20px rgba(212, 175, 55, 0.4);
}

.btn-confirm:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  box-shadow: none;
}
</style>