<template>
  <div class="reply-suggestion-container">
    <div class="suggestion-header">
      <h3>💡 回复建议</h3>
      <el-button
        type="primary"
        size="small"
        :loading="loading"
        @click="generateSuggestion"
        v-if="!suggestions"
      >
        生成建议
      </el-button>
    </div>

    <div v-if="suggestions" class="suggestion-list">
      <div
        v-for="(item, index) in suggestions.suggestions"
        :key="index"
        class="suggestion-item"
        @click="handleCopy(item.content)"
      >
        <div class="tone-tag">{{ item.tone }}</div>
        <div class="content">{{ item.content }}</div>
        <el-icon class="copy-icon"><CopyDocument /></el-icon>
      </div>
    </div>

    <div v-else-if="!loading" class="empty-state">
      <p>点击上方按钮，AI将为你生成回复建议</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { CopyDocument } from '@element-plus/icons-vue'
import { generateReplySuggestion, getReplySuggestion } from '@/api/social'
import { ElMessage } from 'element-plus'

const props = defineProps({
  analysisId: {
    type: [Number, String],
    required: true
  }
})

const loading = ref(false)
const suggestions = ref(null)

const generateSuggestion = async () => {
  loading.value = true
  try {
    const res = await generateReplySuggestion(props.analysisId)
    if (res.code === 200) {
      suggestions.value = JSON.parse(res.data.suggestions)
    } else {
      ElMessage.error(res.message || '生成失败')
    }
  } catch (error) {
    ElMessage.error('生成回复建议失败')
  } finally {
    loading.value = false
  }
}

const fetchSuggestion = async () => {
  try {
    const res = await getReplySuggestion(props.analysisId)
    if (res.code === 200 && res.data) {
      suggestions.value = JSON.parse(res.data.suggestions)
    }
  } catch (error) {
    // 静默失败，可能是还没有生成过
  }
}

const handleCopy = async (content) => {
  try {
    await navigator.clipboard.writeText(content)
    ElMessage.success('已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败，请手动复制')
  }
}

onMounted(() => {
  fetchSuggestion()
})
</script>

<style scoped lang="scss">
.reply-suggestion-container {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  margin-top: 20px;
}

.suggestion-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  h3 {
    font-size: 18px;
    margin: 0;
  }
}

.suggestion-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.suggestion-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #e9ecef;
    transform: translateX(4px);
  }
}

.tone-tag {
  padding: 4px 12px;
  background: #667eea;
  color: white;
  border-radius: 16px;
  font-size: 12px;
  margin-right: 12px;
  white-space: nowrap;
}

.content {
  flex: 1;
  font-size: 14px;
  color: #333;
  line-height: 1.5;
}

.copy-icon {
  color: #999;
  margin-left: 12px;
}

.empty-state {
  text-align: center;
  padding: 24px;
  color: #999;
  font-size: 14px;
}
</style>
