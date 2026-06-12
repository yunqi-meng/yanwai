<template>
  <div class="ranking-container">
    <div class="ranking-header">
      <h2>🏆 排行榜</h2>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="卡牌收集榜" name="cards">
        <div class="ranking-list">
          <div
            v-for="(item, index) in cardRanking"
            :key="item.userId"
            class="ranking-item"
            :class="{ 'top-3': index < 3 }"
          >
            <div class="rank" :class="'rank-' + (index + 1)">
              {{ index < 3 ? ['🥇', '🥈', '🥉'][index] : index + 1 }}
            </div>
            <div class="user-info">
              <span class="nickname">{{ item.nickname }}</span>
              <span class="stat">传说卡: {{ item.legendCount }}</span>
            </div>
            <div class="score">{{ item.totalCards }} 张</div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="分析次数榜" name="analysis">
        <div class="ranking-list">
          <div
            v-for="(item, index) in analysisRanking"
            :key="item.userId"
            class="ranking-item"
            :class="{ 'top-3': index < 3 }"
          >
            <div class="rank" :class="'rank-' + (index + 1)">
              {{ index < 3 ? ['🥇', '🥈', '🥉'][index] : index + 1 }}
            </div>
            <div class="user-info">
              <span class="nickname">{{ item.nickname }}</span>
            </div>
            <div class="score">{{ item.totalAnalysis }} 次</div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="成就榜" name="achievements">
        <div class="ranking-list">
          <div
            v-for="(item, index) in achievementRanking"
            :key="item.userId"
            class="ranking-item"
            :class="{ 'top-3': index < 3 }"
          >
            <div class="rank" :class="'rank-' + (index + 1)">
              {{ index < 3 ? ['🥇', '🥈', '🥉'][index] : index + 1 }}
            </div>
            <div class="user-info">
              <span class="nickname">{{ item.nickname }}</span>
            </div>
            <div class="score">{{ item.achievementCount }} 个</div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCardRanking, getAnalysisRanking, getAchievementRanking } from '@/api/social'

const activeTab = ref('cards')
const cardRanking = ref([])
const analysisRanking = ref([])
const achievementRanking = ref([])

const fetchCardRanking = async () => {
  try {
    const res = await getCardRanking(50)
    if (res.code === 200) {
      cardRanking.value = res.data
    }
  } catch (error) {
    console.error('获取卡牌排行榜失败', error)
  }
}

const fetchAnalysisRanking = async () => {
  try {
    const res = await getAnalysisRanking(50)
    if (res.code === 200) {
      analysisRanking.value = res.data
    }
  } catch (error) {
    console.error('获取分析排行榜失败', error)
  }
}

const fetchAchievementRanking = async () => {
  try {
    const res = await getAchievementRanking(50)
    if (res.code === 200) {
      achievementRanking.value = res.data
    }
  } catch (error) {
    console.error('获取成就排行榜失败', error)
  }
}

const handleTabChange = (tab) => {
  switch (tab) {
    case 'cards':
      if (cardRanking.value.length === 0) fetchCardRanking()
      break
    case 'analysis':
      if (analysisRanking.value.length === 0) fetchAnalysisRanking()
      break
    case 'achievements':
      if (achievementRanking.value.length === 0) fetchAchievementRanking()
      break
  }
}

onMounted(() => {
  fetchCardRanking()
})
</script>

<style scoped lang="scss">
.ranking-container {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
}

.ranking-header {
  text-align: center;
  margin-bottom: 24px;

  h2 {
    font-size: 24px;
  }
}

.ranking-list {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.ranking-item {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  &.top-3 {
    background: linear-gradient(90deg, rgba(255, 215, 0, 0.1) 0%, transparent 100%);
  }
}

.rank {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
  color: #666;
  margin-right: 16px;

  &.rank-1, &.rank-2, &.rank-3 {
    font-size: 24px;
  }
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;

  .nickname {
    font-size: 16px;
    font-weight: 500;
    color: #333;
  }

  .stat {
    font-size: 12px;
    color: #999;
    margin-top: 4px;
  }
}

.score {
  font-size: 18px;
  font-weight: bold;
  color: #667eea;
}
</style>
