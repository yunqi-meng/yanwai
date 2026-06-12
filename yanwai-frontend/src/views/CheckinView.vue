<template>
  <div class="checkin-container">
    <div class="checkin-header">
      <h2>📅 每日签到</h2>
      <p class="subtitle">连续签到获得更多奖励</p>
    </div>

    <div class="checkin-card">
      <div class="checkin-status">
        <div class="streak-info">
          <span class="streak-number">{{ consecutiveDays }}</span>
          <span class="streak-label">连续签到天数</span>
        </div>
        <el-button
          type="primary"
          size="large"
          :disabled="checkedInToday"
          @click="handleCheckin"
          :loading="loading"
        >
          {{ checkedInToday ? '✓ 今日已签到' : '立即签到' }}
        </el-button>
      </div>

      <div class="reward-preview">
        <h4>签到奖励</h4>
        <div class="reward-list">
          <div class="reward-item" :class="{ active: consecutiveDays >= 1 }">
            <span class="day">第1天</span>
            <span class="reward">+1次分析</span>
          </div>
          <div class="reward-item" :class="{ active: consecutiveDays >= 2 }">
            <span class="day">第2天</span>
            <span class="reward">+1次分析</span>
          </div>
          <div class="reward-item" :class="{ active: consecutiveDays >= 3 }">
            <span class="day">第3天</span>
            <span class="reward">+3碎片</span>
          </div>
          <div class="reward-item" :class="{ active: consecutiveDays >= 7 }">
            <span class="day">第7天</span>
            <span class="reward">传说卡牌</span>
          </div>
        </div>
      </div>
    </div>

    <div class="checkin-calendar">
      <h4>本月签到记录</h4>
      <div class="calendar-grid">
        <div
          v-for="day in calendarDays"
          :key="day.date"
          class="calendar-day"
          :class="{ checked: day.checked, today: day.isToday }"
        >
          {{ day.day }}
        </div>
      </div>
    </div>

    <!-- 签到成功弹窗 -->
    <el-dialog v-model="showReward" title="签到成功" width="300px">
      <div class="reward-dialog">
        <div class="reward-icon">🎉</div>
        <p>连续签到 {{ rewardData.consecutiveDays }} 天</p>
        <div class="reward-content">
          <span>{{ rewardText }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { checkin, getCheckinStatus } from '@/api/social'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const checkedInToday = ref(false)
const consecutiveDays = ref(0)
const recentCheckinDates = ref([])
const showReward = ref(false)
const rewardData = ref({})

const rewardText = computed(() => {
  if (!rewardData.value.reward) return ''
  const { type, value } = rewardData.value.reward
  switch (type) {
    case 'analysis': return `+${value}次分析次数`
    case 'fragment': return `+${value}个卡牌碎片`
    case 'card': return '随机传说卡牌!'
    default: return ''
  }
})

const calendarDays = computed(() => {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth()
  const daysInMonth = new Date(year, month + 1, 0).getDate()
  const today = now.getDate()

  const days = []
  for (let i = 1; i <= daysInMonth; i++) {
    const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(i).padStart(2, '0')}`
    days.push({
      day: i,
      date: dateStr,
      checked: recentCheckinDates.value.includes(dateStr),
      isToday: i === today
    })
  }
  return days
})

const fetchStatus = async () => {
  try {
    const res = await getCheckinStatus()
    if (res.code === 200) {
      checkedInToday.value = res.data.checkedInToday
      consecutiveDays.value = res.data.consecutiveDays
      recentCheckinDates.value = res.data.recentCheckinDates || []
    }
  } catch (error) {
    console.error('获取签到状态失败', error)
  }
}

const handleCheckin = async () => {
  loading.value = true
  try {
    const res = await checkin()
    if (res.code === 200) {
      checkedInToday.value = true
      consecutiveDays.value = res.data.consecutiveDays
      rewardData.value = res.data
      showReward.value = true
      fetchStatus()
    } else {
      ElMessage.error(res.message || '签到失败')
    }
  } catch (error) {
    ElMessage.error('签到失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchStatus()
})
</script>

<style scoped lang="scss">
.checkin-container {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
}

.checkin-header {
  text-align: center;
  margin-bottom: 24px;

  h2 {
    font-size: 24px;
    margin-bottom: 8px;
  }

  .subtitle {
    color: #666;
    font-size: 14px;
  }
}

.checkin-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 24px;
  color: white;
  margin-bottom: 24px;
}

.checkin-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.streak-info {
  display: flex;
  flex-direction: column;

  .streak-number {
    font-size: 48px;
    font-weight: bold;
    line-height: 1;
  }

  .streak-label {
    font-size: 14px;
    opacity: 0.9;
    margin-top: 4px;
  }
}

.reward-preview {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 16px;

  h4 {
    margin-bottom: 12px;
    font-size: 14px;
  }
}

.reward-list {
  display: flex;
  justify-content: space-between;
}

.reward-item {
  text-align: center;
  opacity: 0.5;
  transition: all 0.3s;

  &.active {
    opacity: 1;
  }

  .day {
    display: block;
    font-size: 12px;
    margin-bottom: 4px;
  }

  .reward {
    font-size: 12px;
    font-weight: bold;
  }
}

.checkin-calendar {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

  h4 {
    margin-bottom: 16px;
    font-size: 16px;
  }
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
}

.calendar-day {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-size: 14px;
  color: #333;

  &.checked {
    background: #667eea;
    color: white;
  }

  &.today {
    border: 2px solid #667eea;
  }
}

.reward-dialog {
  text-align: center;
  padding: 20px;

  .reward-icon {
    font-size: 48px;
    margin-bottom: 16px;
  }

  .reward-content {
    margin-top: 16px;
    padding: 12px;
    background: #f0f9ff;
    border-radius: 8px;
    font-weight: bold;
    color: #667eea;
  }
}
</style>
