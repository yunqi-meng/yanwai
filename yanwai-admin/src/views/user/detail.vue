<template>
  <div class="user-detail" v-loading="loading">
    <div class="page-header">
      <el-page-header @back="goBack">
        <template #content>
          <span class="back-title">用户详情</span>
        </template>
      </el-page-header>
    </div>

    <el-row :gutter="20" class="detail-content">
      <el-col :span="8">
        <el-card shadow="never" class="info-card">
          <template #header>
            <div class="card-title-row">
              <span>基本信息</span>
              <el-tag size="small" :type="getMemberTagType(user.memberLevel)" effect="light">
                {{ getMemberLabel(user.memberLevel) }}
              </el-tag>
            </div>
          </template>
          <div class="info-list">
            <div class="info-item">
              <span class="info-label">用户ID</span>
              <span class="info-value">{{ user.id }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">昵称</span>
              <span class="info-value">{{ user.nickname || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">OpenID</span>
              <span class="info-value mono">{{ user.openid }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">邮箱</span>
              <span class="info-value">{{ user.email || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">会员到期</span>
              <span class="info-value">{{ user.memberExpireTime ? formatTime(user.memberExpireTime) : '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">注册时间</span>
              <span class="info-value">{{ formatTime(user.createdAt) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">最后登录</span>
              <span class="info-value">{{ user.lastLoginDate || '-' }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card shadow="never" class="stats-card">
          <template #header>
            <span class="card-title-text">统计数据</span>
          </template>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-icon-wrap blue">
                <el-icon size="20"><Document /></el-icon>
              </div>
              <div class="stat-body">
                <div class="stat-num">{{ user.totalAnalysis || 0 }}</div>
                <div class="stat-desc">总分析次数</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon-wrap green">
                <el-icon size="20"><Share /></el-icon>
              </div>
              <div class="stat-body">
                <div class="stat-num">{{ user.totalShare || 0 }}</div>
                <div class="stat-desc">总分享次数</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon-wrap purple">
                <el-icon size="20"><Timer /></el-icon>
              </div>
              <div class="stat-body">
                <div class="stat-num">{{ user.dailyAnalysisCount || 0 }}</div>
                <div class="stat-desc">今日分析</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon-wrap orange">
                <el-icon size="20"><Moon /></el-icon>
              </div>
              <div class="stat-body">
                <div class="stat-num">{{ user.lateNightCount || 0 }}</div>
                <div class="stat-desc">深夜分析</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon-wrap cyan">
                <el-icon size="20"><OfficeBuilding /></el-icon>
              </div>
              <div class="stat-body">
                <div class="stat-num">{{ user.workplaceCount || 0 }}</div>
                <div class="stat-desc">职场分析</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon-wrap pink">
                <el-icon size="20"><MagicStick /></el-icon>
              </div>
              <div class="stat-body">
                <div class="stat-num">{{ user.romanceCount || 0 }}</div>
                <div class="stat-desc">情感分析</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="action-card">
      <template #header>
        <span class="card-title-text">管理操作</span>
      </template>
      <el-space>
        <el-button type="primary" @click="handleEdit">
          <el-icon><Edit /></el-icon>
          编辑信息
        </el-button>
        <el-button
          v-if="user.memberLevel !== -1"
          type="danger"
          @click="handleBan"
        >
          <el-icon><CircleClose /></el-icon>
          封禁用户
        </el-button>
        <el-button
          v-if="user.memberLevel === -1"
          type="success"
          @click="handleUnban"
        >
          <el-icon><CircleCheck /></el-icon>
          解封用户
        </el-button>
        <el-button type="warning" @click="handleMemberDialog">
          <el-icon><Star /></el-icon>
          会员管理
        </el-button>
      </el-space>
    </el-card>

    <el-dialog v-model="editDialogVisible" title="编辑用户" width="480px" :close-on-click-modal="false">
      <el-form :model="editForm" label-width="60px">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="memberDialogVisible" title="会员管理" width="440px" :close-on-click-modal="false">
      <el-form :model="memberForm" label-width="80px">
        <el-form-item label="会员等级">
          <el-select v-model="memberForm.level" style="width: 100%">
            <el-option label="普通用户" :value="0" />
            <el-option label="会员" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="memberForm.level === 1" label="会员天数">
          <el-input-number v-model="memberForm.days" :min="1" :max="365" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="memberDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateMember">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserDetail, updateUser, banUser, unbanUser, updateMemberLevel } from '@/api/user'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const user = ref({})
const editDialogVisible = ref(false)
const memberDialogVisible = ref(false)

const editForm = reactive({
  nickname: ''
})

const memberForm = reactive({
  level: 0,
  days: 30
})

onMounted(() => {
  loadUserDetail()
})

async function loadUserDetail() {
  loading.value = true
  try {
    const res = await getUserDetail(route.params.id)
    user.value = res.data
  } catch (error) {
    console.error('加载用户详情失败:', error)
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push('/user')
}

function handleEdit() {
  editForm.nickname = user.value.nickname
  editDialogVisible.value = true
}

async function handleSaveEdit() {
  try {
    await updateUser(user.value.id, { nickname: editForm.nickname })
    ElMessage.success('保存成功')
    editDialogVisible.value = false
    loadUserDetail()
  } catch (error) {
    console.error('保存失败:', error)
  }
}

async function handleBan() {
  try {
    await ElMessageBox.confirm('确定要封禁该用户吗？封禁后用户将无法使用服务。', '提示', {
      type: 'warning',
      confirmButtonText: '确定封禁',
      cancelButtonText: '取消'
    })
    await banUser(user.value.id)
    ElMessage.success('封禁成功')
    loadUserDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('封禁失败:', error)
    }
  }
}

async function handleUnban() {
  try {
    await unbanUser(user.value.id)
    ElMessage.success('解封成功')
    loadUserDetail()
  } catch (error) {
    console.error('解封失败:', error)
  }
}

function handleMemberDialog() {
  memberForm.level = user.value.memberLevel === -1 ? 0 : user.value.memberLevel
  memberForm.days = 30
  memberDialogVisible.value = true
}

async function handleUpdateMember() {
  try {
    await updateMemberLevel(user.value.id, {
      level: memberForm.level,
      days: memberForm.level === 1 ? memberForm.days : undefined
    })
    ElMessage.success('会员更新成功')
    memberDialogVisible.value = false
    loadUserDetail()
  } catch (error) {
    console.error('更新会员失败:', error)
  }
}

function getMemberTagType(level) {
  const types = { '-1': 'danger', 0: 'info', 1: 'success' }
  return types[level] || 'info'
}

function getMemberLabel(level) {
  const labels = { '-1': '已封禁', 0: '普通用户', 1: '会员' }
  return labels[level] || '未知'
}

function formatTime(time) {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 19)
}
</script>

<style scoped>
.user-detail {
  max-width: 1440px;
}

.page-header {
  margin-bottom: 20px;
}

.back-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-text-primary);
}

.detail-content {
  margin-bottom: 20px;
}

/* ── Info Card ── */
.card-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title-text {
  font-weight: 600;
  font-size: 15px;
}

.info-list {
  display: flex;
  flex-direction: column;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--color-border-light);
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  width: 80px;
  font-size: 13px;
  color: var(--color-text-secondary);
  flex-shrink: 0;
}

.info-value {
  font-size: 14px;
  color: var(--color-text-primary);
  font-weight: 500;
}

.info-value.mono {
  font-family: 'SF Mono', 'Menlo', monospace;
  font-size: 12px;
  color: var(--color-text-secondary);
}

/* ── Stats Grid ── */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px 16px;
  background: var(--color-bg-page);
  border-radius: var(--radius-sm);
  transition: all 0.25s ease;
}

.stat-item:hover {
  background: var(--color-bg-hover);
  transform: translateY(-1px);
}

.stat-icon-wrap {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  flex-shrink: 0;
}

.stat-icon-wrap.blue   { background: #EEF2FF; color: #6366F1; }
.stat-icon-wrap.green  { background: #D1FAE5; color: #059669; }
.stat-icon-wrap.purple { background: #EDE9FE; color: #7C3AED; }
.stat-icon-wrap.orange { background: #FEF3C7; color: #D97706; }
.stat-icon-wrap.cyan   { background: #CFFAFE; color: #0891B2; }
.stat-icon-wrap.pink   { background: #FCE7F3; color: #DB2777; }

.stat-body {
  display: flex;
  flex-direction: column;
}

.stat-num {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1.2;
}

.stat-desc {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-top: 2px;
}

/* ── Action Card ── */
.action-card {
  margin-bottom: 20px;
}
</style>
