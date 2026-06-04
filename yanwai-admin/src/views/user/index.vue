<template>
  <div class="user-container">
    <div class="page-header">
      <div class="page-title">
        <el-icon><User /></el-icon>
        <span>用户管理</span>
      </div>
      <div class="page-desc">管理系统中的所有用户信息</div>
    </div>
    
    <div class="content-card">
      <div class="search-section">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="关键词">
            <el-input
              v-model="searchForm.keyword"
              placeholder="用户名/ID/邮箱"
              clearable
              class="search-input"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="会员状态">
            <el-select v-model="searchForm.memberLevel" placeholder="全部" clearable class="search-select">
              <el-option label="普通用户" :value="0">
                <el-icon><User /></el-icon>
                <span>普通用户</span>
              </el-option>
              <el-option label="会员" :value="1">
                <el-icon><Star /></el-icon>
                <span>会员</span>
              </el-option>
              <el-option label="已封禁" :value="-1">
                <el-icon><CircleClose /></el-icon>
                <span>已封禁</span>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch" class="search-btn">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset" class="reset-btn">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <div class="table-section">
        <el-table :data="userList" stripe v-loading="loading" class="custom-table">
          <el-table-column prop="id" label="ID" width="80">
            <template #default="{ row }">
              <span class="id-badge">#{{ row.id }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="nickname" label="用户信息" min-width="180">
            <template #default="{ row }">
              <div class="user-info-cell">
                <el-avatar :size="40" class="user-avatar">
                  {{ row.nickname?.charAt(0) || 'U' }}
                </el-avatar>
                <div class="user-detail">
                  <div class="user-name">{{ row.nickname }}</div>
                  <div class="user-id">{{ row.openid?.substring(0, 15) }}...</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="memberLevel" label="会员状态" width="120">
            <template #default="{ row }">
              <el-tag 
                :type="getMemberTagType(row.memberLevel)"
                effect="dark"
                round
                class="member-tag"
              >
                <el-icon v-if="row.memberLevel === 1"><Star /></el-icon>
                {{ getMemberLabel(row.memberLevel) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="totalAnalysis" label="分析次数" width="100">
            <template #default="{ row }">
              <span class="number-value">{{ row.totalAnalysis || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="totalShare" label="分享次数" width="100">
            <template #default="{ row }">
              <span class="number-value">{{ row.totalShare || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="注册时间" width="180">
            <template #default="{ row }">
              <div class="time-cell">
                <el-icon><Clock /></el-icon>
                <span>{{ formatTime(row.createdAt) }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button type="primary" size="small" @click="handleViewDetail(row.id)" class="action-btn">
                  <el-icon><View /></el-icon>
                  详情
                </el-button>
                <el-button
                  v-if="row.memberLevel !== -1"
                  type="danger"
                  size="small"
                  @click="handleBan(row.id)"
                  class="action-btn"
                >
                  <el-icon><Lock /></el-icon>
                  封禁
                </el-button>
                <el-button
                  v-if="row.memberLevel === -1"
                  type="success"
                  size="small"
                  @click="handleUnban(row.id)"
                  class="action-btn"
                >
                  <el-icon><Unlock /></el-icon>
                  解封
                </el-button>
                <el-button type="warning" size="small" @click="handleMemberDialog(row)" class="action-btn">
                  <el-icon><Medal /></el-icon>
                  会员
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>
    
    <!-- 会员管理弹窗 -->
    <el-dialog v-model="memberDialogVisible" title="会员管理" width="450px" class="custom-dialog">
      <el-form :model="memberForm" label-width="100px" class="dialog-form">
        <el-form-item label="用户">
          <div class="dialog-user-info">
            <el-avatar :size="36" class="dialog-avatar">
              {{ memberForm.nickname?.charAt(0) || 'U' }}
            </el-avatar>
            <span>{{ memberForm.nickname }}</span>
          </div>
        </el-form-item>
        <el-form-item label="会员等级">
          <el-select v-model="memberForm.level" class="dialog-select">
            <el-option label="普通用户" :value="0" />
            <el-option label="会员" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="memberForm.level === 1" label="会员天数">
          <el-input-number v-model="memberForm.days" :min="1" :max="365" class="dialog-number" />
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, banUser, unbanUser, updateMemberLevel } from '@/api/user'

const router = useRouter()
const loading = ref(false)
const userList = ref([])
const memberDialogVisible = ref(false)

const searchForm = reactive({
  keyword: '',
  memberLevel: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const memberForm = reactive({
  userId: null,
  nickname: '',
  level: 0,
  days: 30
})

onMounted(() => {
  loadUserList()
})

async function loadUserList() {
  loading.value = true
  try {
    const res = await getUserList({
      page: pagination.page,
      size: pagination.size,
      keyword: searchForm.keyword || undefined,
      memberLevel: searchForm.memberLevel
    })
    userList.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error('加载用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  loadUserList()
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.memberLevel = null
  pagination.page = 1
  loadUserList()
}

function handleSizeChange(size) {
  pagination.size = size
  pagination.page = 1
  loadUserList()
}

function handlePageChange(page) {
  pagination.page = page
  loadUserList()
}

function handleViewDetail(id) {
  router.push(`/user/${id}`)
}

async function handleBan(id) {
  try {
    await ElMessageBox.confirm('确定要封禁该用户吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await banUser(id)
    ElMessage.success('封禁成功')
    loadUserList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('封禁失败:', error)
    }
  }
}

async function handleUnban(id) {
  try {
    await unbanUser(id)
    ElMessage.success('解封成功')
    loadUserList()
  } catch (error) {
    console.error('解封失败:', error)
  }
}

function handleMemberDialog(row) {
  memberForm.userId = row.id
  memberForm.nickname = row.nickname
  memberForm.level = row.memberLevel === -1 ? 0 : row.memberLevel
  memberForm.days = 30
  memberDialogVisible.value = true
}

async function handleUpdateMember() {
  try {
    await updateMemberLevel(memberForm.userId, {
      level: memberForm.level,
      days: memberForm.level === 1 ? memberForm.days : undefined
    })
    ElMessage.success('会员更新成功')
    memberDialogVisible.value = false
    loadUserList()
  } catch (error) {
    console.error('更新会员失败:', error)
  }
}

function getMemberTagType(level) {
  const types = { '-1': 'danger', 0: 'info', 1: 'warning' }
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
.user-container {
  padding: 0;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
}

.page-title .el-icon {
  color: #667eea;
  font-size: 28px;
}

.page-desc {
  margin-top: 8px;
  font-size: 14px;
  color: #999;
}

.content-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
}

.search-section {
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.search-form {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.search-input {
  width: 280px;
}

.search-select {
  width: 150px;
}

.search-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.search-btn:hover {
  opacity: 0.9;
}

.reset-btn {
  border-color: #dcdfe6;
}

.table-section {
  margin-bottom: 24px;
}

.custom-table {
  border-radius: 12px;
  overflow: hidden;
}

.custom-table :deep(.el-table__header th) {
  background: #f5f7fa !important;
  color: #666;
  font-weight: 600;
  border-bottom: none;
}

.custom-table :deep(.el-table__row) {
  transition: all 0.3s ease;
}

.custom-table :deep(.el-table__row:hover) {
  background: #f5f7fa !important;
}

.custom-table :deep(.el-table__row td) {
  border-bottom: 1px solid #f0f0f0;
}

.id-badge {
  display: inline-block;
  padding: 4px 10px;
  background: #f0f2f5;
  border-radius: 8px;
  font-size: 13px;
  color: #666;
  font-weight: 600;
}

.user-info-cell {
  display: flex;
  align-items: center;
  gap: 14px;
}

.user-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-weight: 600;
  font-size: 16px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.user-detail {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
}

.user-id {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.member-tag {
  padding: 6px 14px;
  font-size: 13px;
}

.number-value {
  font-weight: 600;
  color: #1a1a2e;
  font-size: 15px;
}

.time-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #999;
  font-size: 13px;
}

.time-cell .el-icon {
  font-size: 14px;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.action-btn {
  font-size: 12px;
  padding: 5px 10px;
  height: 28px;
}

.action-btn .el-icon {
  font-size: 14px;
}

.pagination-section {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

/* 弹窗样式 */
.custom-dialog :deep(.el-dialog) {
  border-radius: 16px;
}

.custom-dialog :deep(.el-dialog__header) {
  padding: 24px 24px 16px;
  border-bottom: 1px solid #f0f0f0;
  margin: 0;
}

.custom-dialog :deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
}

.custom-dialog :deep(.el-dialog__body) {
  padding: 24px;
}

.custom-dialog :deep(.el-dialog__footer) {
  padding: 16px 24px 24px;
  border-top: 1px solid #f0f0f0;
  margin: 0;
}

.dialog-form {
  max-width: 100%;
}

.dialog-user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 10px;
}

.dialog-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-weight: 600;
}

.dialog-select {
  width: 100%;
}

.dialog-number {
  width: 100%;
}
</style>
