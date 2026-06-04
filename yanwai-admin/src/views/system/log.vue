<template>
  <div class="log-container">
    <div class="page-header">
      <div class="page-title">
        <el-icon><Notebook /></el-icon>
        <span>操作日志</span>
      </div>
      <div class="page-desc">查看管理员操作记录</div>
    </div>
    
    <div class="content-card">
      <div class="search-section">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="管理员ID">
            <el-input v-model="searchForm.adminId" placeholder="管理员ID" clearable class="search-input-sm">
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="开始日期">
            <el-date-picker
              v-model="searchForm.startDate"
              type="date"
              placeholder="选择日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              class="date-picker"
            />
          </el-form-item>
          <el-form-item label="结束日期">
            <el-date-picker
              v-model="searchForm.endDate"
              type="date"
              placeholder="选择日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              class="date-picker"
            />
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
        <el-table :data="logList" stripe v-loading="loading" class="custom-table">
          <el-table-column prop="id" label="ID" width="80">
            <template #default="{ row }">
              <span class="id-badge">#{{ row.id }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="adminId" label="管理员" width="120">
            <template #default="{ row }">
              <div class="admin-cell">
                <el-avatar :size="32" class="admin-avatar">
                  {{ row.adminId }}
                </el-avatar>
                <span class="admin-id">管理员{{ row.adminId }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="operation" label="操作" width="150">
            <template #default="{ row }">
              <el-tag :type="getOperationTagType(row.operation)" effect="dark" round class="operation-tag">
                {{ row.operation }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="method" label="方法" width="80">
            <template #default="{ row }">
              <el-tag effect="plain" class="method-tag">{{ row.method }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="params" label="参数" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="params-text">{{ row.params || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="ip" label="IP地址" width="150">
            <template #default="{ row }">
              <div class="ip-cell">
                <el-icon><Location /></el-icon>
                <span>{{ row.ip }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="操作时间" width="180">
            <template #default="{ row }">
              <div class="time-cell">
                <el-icon><Clock /></el-icon>
                <span>{{ formatTime(row.createdAt) }}</span>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getLogList } from '@/api/system'

const loading = ref(false)
const logList = ref([])

const searchForm = reactive({
  adminId: '',
  startDate: '',
  endDate: ''
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

onMounted(() => {
  loadLogList()
})

async function loadLogList() {
  loading.value = true
  try {
    const res = await getLogList({
      page: pagination.page,
      size: pagination.size,
      adminId: searchForm.adminId || undefined,
      startDate: searchForm.startDate || undefined,
      endDate: searchForm.endDate || undefined
    })
    logList.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error('加载日志列表失败:', error)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  loadLogList()
}

function handleReset() {
  searchForm.adminId = ''
  searchForm.startDate = ''
  searchForm.endDate = ''
  pagination.page = 1
  loadLogList()
}

function handleSizeChange(size) {
  pagination.size = size
  pagination.page = 1
  loadLogList()
}

function handlePageChange(page) {
  pagination.page = page
  loadLogList()
}

function formatTime(time) {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 19)
}

function getOperationTagType(operation) {
  if (operation.includes('删除')) return 'danger'
  if (operation.includes('封禁')) return 'warning'
  if (operation.includes('创建')) return 'success'
  return 'primary'
}
</script>

<style scoped>
.log-container {
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

.search-input-sm {
  width: 160px;
}

.date-picker {
  width: 160px;
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

.admin-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.admin-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-weight: 600;
  font-size: 12px;
}

.admin-id {
  font-size: 13px;
  color: #666;
}

.operation-tag {
  padding: 6px 14px;
  font-size: 13px;
}

.method-tag {
  font-family: 'Courier New', monospace;
  font-weight: 600;
}

.params-text {
  font-size: 13px;
  color: #666;
  font-family: 'Courier New', monospace;
}

.ip-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #666;
  font-size: 13px;
}

.ip-cell .el-icon {
  color: #999;
  font-size: 14px;
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

.pagination-section {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}
</style>
