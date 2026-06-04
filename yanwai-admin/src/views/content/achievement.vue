<template>
  <div class="achievement-container">
    <div class="page-header">
      <div class="page-title">
        <el-icon><Trophy /></el-icon>
        <span>成就管理</span>
      </div>
      <div class="page-desc">管理游戏中的所有成就</div>
    </div>
    
    <div class="content-card">
      <div class="toolbar-section">
        <div class="toolbar-left">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索成就名称"
            clearable
            class="search-input"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        <el-button type="primary" @click="handleAdd" class="add-btn">
          <el-icon><Plus /></el-icon>
          新增成就
        </el-button>
      </div>
      
      <div class="table-section">
        <el-table :data="achievementList" stripe v-loading="loading" class="custom-table">
          <el-table-column prop="id" label="ID" width="80">
            <template #default="{ row }">
              <span class="id-badge">#{{ row.id }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="icon" label="图标" width="80">
            <template #default="{ row }">
              <div class="icon-cell">
                <span class="icon">{{ row.icon }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="名称" width="140">
            <template #default="{ row }">
              <span class="achievement-name">{{ row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="code" label="编码" width="150">
            <template #default="{ row }">
              <el-tag effect="plain" class="code-tag">{{ row.code }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" show-overflow-tooltip />
          <el-table-column label="条件" width="200">
            <template #default="{ row }">
              <div class="condition-cell">
                <span class="condition-field">{{ row.conditionField }}</span>
                <span class="condition-op">{{ row.conditionOp }}</span>
                <span class="condition-value">{{ row.conditionValue }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="奖励" width="150">
            <template #default="{ row }">
              <div class="reward-cell">
                <el-tag :type="getRewardTagType(row.rewardType)" effect="dark" round>
                  {{ row.rewardType }}
                </el-tag>
                <span class="reward-value">{{ row.rewardValue }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button type="primary" link @click="handleEdit(row)" class="action-btn">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-popconfirm title="确定删除该成就吗？" @confirm="handleDelete(row.id)">
                  <template #reference>
                    <el-button type="danger" link class="action-btn">
                      <el-icon><Delete /></el-icon>
                      删除
                    </el-button>
                  </template>
                </el-popconfirm>
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
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>
    
    <!-- 成就编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑成就' : '新增成就'"
      width="650px"
      class="custom-dialog"
    >
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px" class="dialog-form">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="名称" prop="name">
              <el-input v-model="formData.name" placeholder="请输入成就名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="编码" prop="code">
              <el-input v-model="formData.code" placeholder="请输入唯一编码" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="图标" prop="icon">
              <el-input v-model="formData.icon" placeholder="请输入emoji图标" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="描述" prop="description">
              <el-input v-model="formData.description" placeholder="请输入成就描述" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-divider content-position="left">
          <el-icon><Setting /></el-icon>
          达成条件
        </el-divider>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="统计字段" prop="conditionField">
              <el-select v-model="formData.conditionField" class="dialog-select">
                <el-option label="总分析次数" value="total_analysis" />
                <el-option label="总分享次数" value="total_share" />
                <el-option label="深夜分析次数" value="late_night_count" />
                <el-option label="职场分析次数" value="workplace_count" />
                <el-option label="情感分析次数" value="romance_count" />
                <el-option label="登录天数" value="login_days" />
                <el-option label="传说卡牌数" value="legend_count" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="操作符" prop="conditionOp">
              <el-select v-model="formData.conditionOp" class="dialog-select">
                <el-option label=">=" value=">=" />
                <el-option label=">" value=">" />
                <el-option label="=" value="=" />
                <el-option label="<" value="<" />
                <el-option label="<=" value="<=" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="阈值" prop="conditionValue">
              <el-input-number v-model="formData.conditionValue" :min="0" class="dialog-number" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-divider content-position="left">
          <el-icon><Present /></el-icon>
          奖励设置
        </el-divider>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="奖励类型" prop="rewardType">
              <el-select v-model="formData.rewardType" class="dialog-select">
                <el-option label="称号" value="称号" />
                <el-option label="次数" value="次数" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="奖励内容" prop="rewardValue">
              <el-input v-model="formData.rewardValue" placeholder="请输入奖励内容" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAchievementList, createAchievement, updateAchievement, deleteAchievement } from '@/api/game'

const loading = ref(false)
const achievementList = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)
const searchKeyword = ref('')

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const formData = reactive({
  name: '',
  code: '',
  icon: '',
  description: '',
  conditionField: 'total_analysis',
  conditionOp: '>=',
  conditionValue: 0,
  rewardType: '称号',
  rewardValue: ''
})

const rules = {
  name: [{ required: true, message: '请输入成就名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入唯一编码', trigger: 'blur' }],
  conditionField: [{ required: true, message: '请选择统计字段', trigger: 'change' }],
  conditionValue: [{ required: true, message: '请输入阈值', trigger: 'blur' }]
}

onMounted(() => {
  loadAchievementList()
})

async function loadAchievementList() {
  loading.value = true
  try {
    const res = await getAchievementList({
      page: pagination.page,
      size: pagination.size
    })
    achievementList.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error('加载成就列表失败:', error)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  loadAchievementList()
}

function handleSizeChange(size) {
  pagination.size = size
  pagination.page = 1
  loadAchievementList()
}

function handlePageChange(page) {
  pagination.page = page
  loadAchievementList()
}

function handleAdd() {
  isEdit.value = false
  editId.value = null
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  editId.value = row.id
  Object.assign(formData, {
    name: row.name,
    code: row.code,
    icon: row.icon,
    description: row.description,
    conditionField: row.conditionField,
    conditionOp: row.conditionOp,
    conditionValue: row.conditionValue,
    rewardType: row.rewardType,
    rewardValue: row.rewardValue
  })
  dialogVisible.value = true
}

async function handleDelete(id) {
  try {
    await deleteAchievement(id)
    ElMessage.success('删除成功')
    loadAchievementList()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  try {
    if (isEdit.value) {
      await updateAchievement(editId.value, formData)
      ElMessage.success('更新成功')
    } else {
      await createAchievement(formData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadAchievementList()
  } catch (error) {
    console.error('提交失败:', error)
  }
}

function resetForm() {
  Object.assign(formData, {
    name: '',
    code: '',
    icon: '',
    description: '',
    conditionField: 'total_analysis',
    conditionOp: '>=',
    conditionValue: 0,
    rewardType: '称号',
    rewardValue: ''
  })
}

function getRewardTagType(type) {
  return type === '称号' ? 'warning' : 'success'
}
</script>

<style scoped>
.achievement-container {
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
  color: #E6A23C;
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

.toolbar-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-input {
  width: 280px;
}

.add-btn {
  background: linear-gradient(135deg, #E6A23C 0%, #f0c78a 100%);
  border: none;
  padding: 10px 20px;
  border-radius: 10px;
  font-weight: 600;
}

.add-btn:hover {
  opacity: 0.9;
  transform: translateY(-1px);
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

.icon-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon {
  font-size: 28px;
  line-height: 1;
}

.achievement-name {
  font-weight: 600;
  color: #1a1a2e;
}

.code-tag {
  font-family: 'Courier New', monospace;
}

.condition-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.condition-field {
  color: #667eea;
  font-weight: 600;
}

.condition-op {
  color: #999;
}

.condition-value {
  color: #E6A23C;
  font-weight: 600;
}

.reward-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.reward-value {
  font-size: 13px;
  color: #666;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
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

.dialog-select {
  width: 100%;
}

.dialog-number {
  width: 100%;
}

:deep(.el-divider) {
  margin: 24px 0;
}

:deep(.el-divider__text) {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #667eea;
  font-weight: 600;
}
</style>
