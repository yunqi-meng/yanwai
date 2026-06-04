<template>
  <div class="card-container">
    <div class="page-header">
      <div class="page-title">
        <el-icon><Postcard /></el-icon>
        <span>卡牌管理</span>
      </div>
      <div class="page-desc">管理游戏中的所有卡牌</div>
    </div>
    
    <div class="content-card">
      <div class="toolbar-section">
        <div class="toolbar-left">
          <el-select v-model="searchRarity" placeholder="全部稀有度" clearable @change="handleSearch" class="rarity-select">
            <el-option label="全部" value="" />
            <el-option label="普通" :value="1">
              <span class="rarity-option rarity-normal">普通</span>
            </el-option>
            <el-option label="稀有" :value="2">
              <span class="rarity-option rarity-rare">稀有</span>
            </el-option>
            <el-option label="史诗" :value="3">
              <span class="rarity-option rarity-epic">史诗</span>
            </el-option>
            <el-option label="传说" :value="4">
              <span class="rarity-option rarity-legend">传说</span>
            </el-option>
          </el-select>
        </div>
        <el-button type="primary" @click="handleAdd" class="add-btn">
          <el-icon><Plus /></el-icon>
          新增卡牌
        </el-button>
      </div>
      
      <div class="table-section">
        <el-table :data="cardList" stripe v-loading="loading" class="custom-table">
          <el-table-column prop="id" label="ID" width="80">
            <template #default="{ row }">
              <span class="id-badge">#{{ row.id }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="emoji" label="图标" width="80">
            <template #default="{ row }">
              <div class="emoji-cell">
                <span class="emoji">{{ row.emoji }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="名称" width="140">
            <template #default="{ row }">
              <span class="card-name">{{ row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="rarity" label="稀有度" width="100">
            <template #default="{ row }">
              <el-tag 
                :type="getRarityTagType(row.rarity)"
                effect="dark"
                round
                class="rarity-tag"
              >
                {{ getRarityLabel(row.rarity) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="personalityType" label="人格类型" width="120">
            <template #default="{ row }">
              <el-tag effect="plain" round>{{ row.personalityType || '-' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="trait" label="特质" width="120" />
          <el-table-column prop="description" label="描述" show-overflow-tooltip />
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button type="primary" link @click="handleEdit(row)" class="action-btn">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-popconfirm title="确定删除该卡牌吗？" @confirm="handleDelete(row.id)">
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
    
    <!-- 卡牌编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑卡牌' : '新增卡牌'"
      width="600px"
      class="custom-dialog"
    >
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px" class="dialog-form">
        <el-form-item label="名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入卡牌名称" />
        </el-form-item>
        <el-form-item label="图标" prop="emoji">
          <el-input v-model="formData.emoji" placeholder="请输入emoji图标" />
        </el-form-item>
        <el-form-item label="稀有度" prop="rarity">
          <el-select v-model="formData.rarity" class="dialog-select">
            <el-option label="普通" :value="1" />
            <el-option label="稀有" :value="2" />
            <el-option label="史诗" :value="3" />
            <el-option label="传说" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="人格类型" prop="personalityType">
          <el-input v-model="formData.personalityType" placeholder="请输入人格类型" />
        </el-form-item>
        <el-form-item label="特质" prop="trait">
          <el-input v-model="formData.trait" placeholder="请输入特质描述" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入卡牌描述" />
        </el-form-item>
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
import { getCardList, createCard, updateCard, deleteCard } from '@/api/game'

const loading = ref(false)
const cardList = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)
const searchRarity = ref(null)

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const formData = reactive({
  name: '',
  emoji: '',
  rarity: 1,
  personalityType: '',
  trait: '',
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入卡牌名称', trigger: 'blur' }],
  emoji: [{ required: true, message: '请输入emoji图标', trigger: 'blur' }],
  rarity: [{ required: true, message: '请选择稀有度', trigger: 'change' }]
}

onMounted(() => {
  loadCardList()
})

async function loadCardList() {
  loading.value = true
  try {
    const res = await getCardList({
      page: pagination.page,
      size: pagination.size,
      rarity: searchRarity.value || undefined
    })
    cardList.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error('加载卡牌列表失败:', error)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  loadCardList()
}

function handleSizeChange(size) {
  pagination.size = size
  pagination.page = 1
  loadCardList()
}

function handlePageChange(page) {
  pagination.page = page
  loadCardList()
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
    emoji: row.emoji,
    rarity: row.rarity,
    personalityType: row.personalityType,
    trait: row.trait,
    description: row.description
  })
  dialogVisible.value = true
}

async function handleDelete(id) {
  try {
    await deleteCard(id)
    ElMessage.success('删除成功')
    loadCardList()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  try {
    if (isEdit.value) {
      await updateCard(editId.value, formData)
      ElMessage.success('更新成功')
    } else {
      await createCard(formData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadCardList()
  } catch (error) {
    console.error('提交失败:', error)
  }
}

function resetForm() {
  Object.assign(formData, {
    name: '',
    emoji: '',
    rarity: 1,
    personalityType: '',
    trait: '',
    description: ''
  })
}

function getRarityTagType(rarity) {
  const types = { 1: 'info', 2: 'success', 3: 'warning', 4: 'danger' }
  return types[rarity] || 'info'
}

function getRarityLabel(rarity) {
  const labels = { 1: '普通', 2: '稀有', 3: '史诗', 4: '传说' }
  return labels[rarity] || '未知'
}
</script>

<style scoped>
.card-container {
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

.rarity-select {
  width: 160px;
}

.rarity-option {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
}

.rarity-normal {
  color: #909399;
}

.rarity-rare {
  color: #67C23A;
}

.rarity-epic {
  color: #E6A23C;
}

.rarity-legend {
  color: #F56C6C;
}

.add-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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

.emoji-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}

.emoji {
  font-size: 28px;
  line-height: 1;
}

.card-name {
  font-weight: 600;
  color: #1a1a2e;
}

.rarity-tag {
  padding: 6px 14px;
  font-size: 13px;
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
</style>
