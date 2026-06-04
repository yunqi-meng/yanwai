<template>
  <div class="app-container">
    <div class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>
    <nav class="tab-bar">
      <div
        v-for="tab in tabs"
        :key="tab.path"
        class="tab-item"
        :class="{ active: currentPath === tab.path }"
        @click="goTo(tab.path)"
      >
        <span class="icon">{{ tab.icon }}</span>
        <span class="label">{{ tab.label }}</span>
      </div>
    </nav>

    <div class="loading-overlay" v-if="analysisStore.isAnalyzing && analysisStore.analysisType === 'text'">
      <div class="loading-animation">
        <span class="icon">🔮</span>
      </div>
      <div class="loading-text">正在解读潜台词...</div>
      <div class="loading-progress">
        <div class="progress-bar">
          <div class="progress-fill" :style="{width: analysisStore.loadingProgress + '%'}"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from './stores/user'
import { useAnalysisStore } from './stores/analysis'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const analysisStore = useAnalysisStore()

const tabs = [
  { path: '/home', label: '解码', icon: '🔮' },
  { path: '/collection', label: '收藏', icon: '📚' },
  { path: '/profile', label: '我的', icon: '👤' }
]

const currentPath = computed(() => route.path)

function goTo(path) {
  if (analysisStore.isAnalyzing && analysisStore.analysisType === 'text') return
  router.push(path)
}

watch(() => analysisStore.isAnalyzing, (analyzing) => {
  if (!analyzing && analysisStore.result && route.path === '/home') {
    router.push({
      path: '/result',
      query: { timestamp: Date.now() }
    })
  }
})

onMounted(async () => {
  if (userStore.userId) {
    await userStore.fetchStats()
  }
})
</script>

<style scoped>
.app-container {
  min-height: 100vh;
  background: var(--bg-page);
  position: relative;
  overflow-x: hidden;
  transition: background 0.3s ease;
}

.app-container::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 20% 0%, rgba(212, 175, 55, 0.05) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

.main-content {
  padding-bottom: 80px;
  position: relative;
  z-index: 1;
}

.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateX(-20px);
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
  color: #D4AF37;
  font-size: 20px;
  font-weight: 700;
  margin-top: 24px;
  letter-spacing: 1px;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 0.8; }
  50% { opacity: 1; }
}

.loading-progress {
  width: 200px;
  margin-top: 24px;
}

.progress-bar {
  width: 100%;
  height: 6px;
  background: rgba(212, 175, 55, 0.15);
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #B8860B, #D4AF37, #F5D061);
  border-radius: 3px;
  transition: width 0.3s ease;
  box-shadow: 0 0 10px rgba(212, 175, 55, 0.4);
}
</style>
