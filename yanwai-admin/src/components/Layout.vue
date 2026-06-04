<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '240px'" class="aside">
      <div class="logo" :class="{ 'logo-collapse': isCollapse }">
        <div class="logo-icon">🔮</div>
        <transition name="fade">
          <div v-if="!isCollapse" class="logo-text">
            <span class="logo-title">言外</span>
            <span class="logo-subtitle">管理后台</span>
          </div>
        </transition>
      </div>

      <el-scrollbar class="menu-scrollbar">
        <el-menu
          :default-active="currentPath"
          :collapse="isCollapse"
          router
          :collapse-transition="false"
          class="side-menu"
          background-color="transparent"
          text-color="rgba(255,255,255,0.7)"
          active-text-color="#fff"
        >
          <el-menu-item index="/dashboard" class="menu-item">
            <el-icon><DataBoard /></el-icon>
            <template #title>
              <span>数据看板</span>
            </template>
          </el-menu-item>

          <el-menu-item index="/user" class="menu-item">
            <el-icon><User /></el-icon>
            <template #title>
              <span>用户管理</span>
            </template>
          </el-menu-item>

          <el-sub-menu index="content" popper-class="sub-menu-popper">
            <template #title>
              <el-icon><Grid /></el-icon>
              <span>内容管理</span>
            </template>
            <el-menu-item index="/content/card">
              <el-icon><Postcard /></el-icon>
              <span>卡牌管理</span>
            </el-menu-item>
            <el-menu-item index="/content/achievement">
              <el-icon><Trophy /></el-icon>
              <span>成就管理</span>
            </el-menu-item>
          </el-sub-menu>

          <el-menu-item index="/analysis" class="menu-item">
            <el-icon><Document /></el-icon>
            <template #title>
              <span>分析记录</span>
            </template>
          </el-menu-item>

          <el-sub-menu index="system" popper-class="sub-menu-popper">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/system/config">
              <el-icon><Tools /></el-icon>
              <span>系统配置</span>
            </el-menu-item>
            <el-menu-item index="/system/log">
              <el-icon><Notebook /></el-icon>
              <span>操作日志</span>
            </el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <div class="collapse-btn" @click="isCollapse = !isCollapse">
            <el-icon :size="20">
              <Fold v-if="!isCollapse" />
              <Expand v-else />
            </el-icon>
          </div>

          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/' }">
              <el-icon><HomeFilled /></el-icon>
            </el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRoute.meta.title">
              {{ currentRoute.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <div class="header-action" @click="toggleFullscreen">
            <el-icon :size="18"><FullScreen /></el-icon>
          </div>

          <el-dropdown @command="handleCommand" class="user-dropdown">
            <div class="user-info">
              <el-avatar :size="36" class="user-avatar">
                {{ adminStore.adminInfo?.realName?.charAt(0) || 'A' }}
              </el-avatar>
              <div class="user-detail">
                <span class="user-name">{{ adminStore.adminInfo?.realName || '管理员' }}</span>
                <span class="user-role">{{ adminStore.adminInfo?.role === 2 ? '超级管理员' : '管理员' }}</span>
              </div>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人信息
                </el-dropdown-item>
                <el-dropdown-item command="password">
                  <el-icon><Lock /></el-icon>
                  修改密码
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/admin'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const adminStore = useAdminStore()
const isCollapse = ref(false)

const currentPath = computed(() => route.path)
const currentRoute = computed(() => route)

onMounted(async () => {
  await adminStore.fetchAdminInfo()
})

function toggleFullscreen() {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

function handleCommand(command) {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      adminStore.logout()
      router.push('/login')
    }).catch(() => {})
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

/* ========== Aside 侧边栏 ========== */
.aside {
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  transition: width 0.3s ease;
  overflow: hidden;
  box-shadow: 2px 0 15px rgba(0, 0, 0, 0.1);
}

.logo {
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 20px;
  background: rgba(255, 255, 255, 0.05);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  transition: all 0.3s ease;
}

.logo-collapse {
  padding: 0;
}

.logo-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.logo-text {
  margin-left: 12px;
  display: flex;
  flex-direction: column;
  white-space: nowrap;
}

.logo-title {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  line-height: 1.2;
}

.logo-subtitle {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
}

.menu-scrollbar {
  height: calc(100vh - 70px);
}

/* ========== 菜单基础 ========== */
.side-menu {
  border: none;
  padding: 10px 0;
}

.side-menu :deep(.el-menu) {
  background: transparent !important;
}

/* ========== 一级菜单项 ========== */
.side-menu :deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
  margin: 4px 12px;
  border-radius: 12px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
  transition: all 0.3s ease;
}

.side-menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.08) !important;
  color: #fff !important;
}

.side-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  color: #fff !important;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

/* ========== SubMenu - 父级标题 ========== */
.side-menu :deep(.el-sub-menu__title) {
  height: 50px;
  line-height: 50px;
  margin: 4px 12px;
  border-radius: 12px;
  color: rgba(255, 255, 255, 0.7) !important;
  font-size: 14px;
  transition: all 0.3s ease;
}

.side-menu :deep(.el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, 0.08) !important;
  color: #fff !important;
}

.side-menu :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  color: #fff !important;
}

.side-menu :deep(.el-sub-menu.is-opened > .el-sub-menu__title) {
  color: #fff !important;
}

/* ========== SubMenu - 子菜单容器 ========== */
.side-menu :deep(.el-menu--inline) {
  background: rgba(0, 0, 0, 0.15) !important;
  margin: 0 12px;
  border-radius: 0 0 12px 12px;
  padding: 4px 0;
}

/* ========== SubMenu - 子菜单项 ========== */
.side-menu :deep(.el-sub-menu .el-menu-item) {
  height: 44px;
  line-height: 44px;
  margin: 2px 8px;
  padding-left: 56px !important;
  border-radius: 10px;
  color: rgba(255, 255, 255, 0.6) !important;
  font-size: 13px;
}

.side-menu :deep(.el-sub-menu .el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.06) !important;
  color: #fff !important;
}

.side-menu :deep(.el-sub-menu .el-menu-item.is-active) {
  background: rgba(102, 126, 234, 0.3) !important;
  color: #fff !important;
}

/* ========== 菜单图标 ========== */
.side-menu :deep(.el-menu-item .el-icon),
.side-menu :deep(.el-sub-menu__title .el-icon) {
  font-size: 18px;
  margin-right: 10px;
}

/* ========== SubMenu 箭头 ========== */
.side-menu :deep(.el-sub-menu__icon-arrow) {
  color: rgba(255, 255, 255, 0.4) !important;
  font-size: 12px;
  right: 16px;
  margin-top: -4px;
}

/* ========== Header ========== */
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  box-shadow: 0 2px 15px rgba(0, 0, 0, 0.05);
  padding: 0 24px;
  height: 70px;
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.collapse-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  cursor: pointer;
  color: #666;
  transition: all 0.3s ease;
}

.collapse-btn:hover {
  background: #f5f7fa;
  color: #667eea;
}

.breadcrumb {
  font-size: 14px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.header-action {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  cursor: pointer;
  color: #666;
  transition: all 0.3s ease;
}

.header-action:hover {
  background: #f5f7fa;
  color: #667eea;
}

.user-dropdown {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 12px;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.user-info:hover {
  background: #f5f7fa;
}

.user-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-weight: 600;
  font-size: 14px;
}

.user-detail {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  line-height: 1.2;
}

.user-role {
  font-size: 11px;
  color: #999;
}

.dropdown-icon {
  color: #999;
  font-size: 12px;
}

/* ========== Main Content ========== */
.main {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

/* ========== Animation ========== */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s ease;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(20px);
}
</style>

<style>
/* 全局样式：SubMenu 折叠态弹出菜单 */
.sub-menu-popper {
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%) !important;
  border: 1px solid rgba(255, 255, 255, 0.08) !important;
  border-radius: 12px !important;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.3) !important;
  padding: 4px 0 !important;
}

.sub-menu-popper .el-menu {
  background: transparent !important;
  border: none !important;
}

.sub-menu-popper .el-menu-item {
  height: 44px;
  line-height: 44px;
  margin: 2px 8px;
  border-radius: 10px;
  color: rgba(255, 255, 255, 0.7) !important;
  font-size: 13px;
  transition: all 0.3s ease;
}

.sub-menu-popper .el-menu-item:hover {
  background: rgba(255, 255, 255, 0.08) !important;
  color: #fff !important;
}

.sub-menu-popper .el-menu-item.is-active {
  background: rgba(102, 126, 234, 0.3) !important;
  color: #fff !important;
}
</style>
