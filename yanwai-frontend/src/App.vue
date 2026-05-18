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
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from './stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const tabs = [
  { path: '/', label: '解码', icon: '🔮' },
  { path: '/collection', label: '收藏', icon: '📚' },
  { path: '/profile', label: '我的', icon: '👤' }
]

const currentPath = computed(() => route.path)

function goTo(path) {
  router.push(path)
}

onMounted(async () => {
  await userStore.login()
})
</script>

<style scoped>
.app-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #0B0E14 0%, #0A0D12 100%);
  position: relative;
  overflow-x: hidden;
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

.tab-bar {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 430px;
  height: 70px;
  background: linear-gradient(180deg, rgba(30, 41, 59, 0.98) 0%, rgba(18, 24, 38, 0.98) 100%);
  border-top: 1px solid rgba(212, 175, 55, 0.2);
  display: flex;
  justify-content: space-around;
  align-items: center;
  z-index: 100;
  backdrop-filter: blur(20px);
  box-shadow: 0 -4px 30px rgba(0, 0, 0, 0.4);
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #94A3B8;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 10px 20px;
  border-radius: 14px;
  position: relative;
}

.tab-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 3px;
  background: linear-gradient(90deg, #B8860B, #D4AF37, #F5D061);
  border-radius: 0 0 3px 3px;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.tab-item:hover {
  color: #D4AF37;
  background: rgba(212, 175, 55, 0.05);
}

.tab-item.active {
  color: #D4AF37;
}

.tab-item.active::before {
  width: 30px;
}

.tab-item .icon {
  font-size: 26px;
  margin-bottom: 4px;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.tab-item:hover .icon {
  transform: translateY(-2px) scale(1.1);
}

.tab-item.active .icon {
  transform: translateY(-3px) scale(1.15);
  filter: drop-shadow(0 0 10px rgba(212, 175, 55, 0.5));
}

.tab-item .label {
  font-weight: 600;
  letter-spacing: 0.3px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.tab-item.active .label {
  font-weight: 700;
}
</style>
