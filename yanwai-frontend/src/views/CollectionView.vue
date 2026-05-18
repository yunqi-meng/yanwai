<template>
  <div class="collection-container">
    <div class="header-section">
      <h1>🎴 我的收藏</h1>
      <div class="tab-switcher">
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'cards' }"
          @click="activeTab = 'cards'"
        >
          图鉴
        </button>
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'achievements' }"
          @click="activeTab = 'achievements'"
        >
          成就
        </button>
      </div>
    </div>

    <div v-if="activeTab === 'cards'" class="content-section">
      <div class="stats-card">
        <div class="stat-item">
          <span class="stat-icon">🎴</span>
          <span class="stat-label">收集进度</span>
          <span class="stat-value">{{ userCards.length }}/{{ totalCards }}</span>
        </div>
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: (userCards.length / totalCards * 100) + '%' }"></div>
        </div>
      </div>

      <div class="hint-text">
        <span class="hint-icon">💡</span>
        <span>仅能通过解码潜台词获得</span>
      </div>

      <div class="rarity-filters">
        <button
          v-for="filter in rarityFilters"
          :key="filter.value"
          class="filter-btn"
          :class="{ active: currentFilter === filter.value }"
          @click="currentFilter = filter.value"
        >
          {{ filter.label }}
        </button>
      </div>

      <div class="cards-grid">
        <div
          v-for="card in filteredCards"
          :key="card.id"
          class="card-item"
          :class="['rarity-' + card.rarity, { locked: !card.owned }]"
          @click="showCardDetail(card)"
        >
          <div class="card-inner">
            <span class="card-emoji">{{ card.emoji }}</span>
            <span class="card-name">{{ card.owned ? card.name : '???' }}</span>
            <span class="card-count" v-if="card.owned && card.quantity > 1">×{{ card.quantity }}</span>
          </div>
          <div class="rarity-badge">{{ getRarityLabel(card.rarity) }}</div>
        </div>
      </div>
    </div>

    <div v-else class="content-section">
      <div class="achievement-stats">
        <div class="stat-item">
          <span class="stat-icon">🏆</span>
          <span class="stat-label">已解锁</span>
          <span class="stat-value">{{ unlockedCount }}/{{ totalAchievements }}</span>
        </div>
        <div class="progress-bar">
          <div class="progress-fill achievement" :style="{ width: (unlockedCount / totalAchievements * 100) + '%' }"></div>
        </div>
      </div>

      <div class="achievements-list">
        <div
          v-for="achievement in allAchievements"
          :key="achievement.id"
          class="achievement-item"
          :class="{ unlocked: isAchievementUnlocked(achievement.code) }"
          @click="showAchievementDetail(achievement)"
        >
          <div class="achievement-badge" :class="{ locked: !isAchievementUnlocked(achievement.code) }">
            <span class="badge-icon">{{ achievement.icon }}</span>
          </div>
          <div class="achievement-info">
            <span class="achievement-name">{{ achievement.name }}</span>
            <span class="achievement-desc">{{ achievement.description }}</span>
          </div>
          <div class="achievement-status">
            <span v-if="isAchievementUnlocked(achievement.code)" class="status-unlocked">✓</span>
            <span v-else class="status-locked">🔒</span>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="showCardDialog" :title="selectedCard?.name" width="85%" class="card-dialog">
      <div v-if="selectedCard" class="card-detail">
        <div class="card-showcase" :class="'rarity-' + selectedCard.rarity">
          <span class="showcase-emoji">{{ selectedCard.emoji }}</span>
          <span class="showcase-rarity">{{ getRarityLabel(selectedCard.rarity) }}</span>
        </div>
        <div class="card-description">
          <p>{{ selectedCard.description }}</p>
        </div>
        <div class="card-meta">
          <span>拥有数量: {{ selectedCard.quantity || 0 }}</span>
          <span>稀有度: {{ getRarityLabel(selectedCard.rarity) }}</span>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="showAchievementDialog" :title="selectedAchievement?.name" width="85%" class="achievement-dialog">
      <div v-if="selectedAchievement" class="achievement-detail">
        <div class="achievement-showcase">
          <span class="showcase-icon">{{ selectedAchievement.icon }}</span>
          <span class="showcase-status">
            {{ isAchievementUnlocked(selectedAchievement.code) ? '已解锁' : '未解锁' }}
          </span>
        </div>
        <div class="achievement-description">
          <p>{{ selectedAchievement.description }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { cardsApi, achievementsApi } from '../api'

const activeTab = ref('cards')
const currentFilter = ref('all')
const showCardDialog = ref(false)
const showAchievementDialog = ref(false)
const selectedCard = ref(null)
const selectedAchievement = ref(null)

const allCards = ref([])
const allAchievements = ref([])
const userOwnedCards = ref([])
const unlockedAchievementCodes = ref([])

const rarityFilters = [
  { label: '全部', value: 'all' },
  { label: '普通', value: 1 },
  { label: '稀有', value: 2 },
  { label: '史诗', value: 3 },
  { label: '传说', value: 4 }
]

const userCards = computed(() => {
  return allCards.value.filter(card => 
    userOwnedCards.value.some(owned => owned.cardId === card.id && owned.quantity > 0)
  )
})

const totalCards = computed(() => allCards.value.length)

const filteredCards = computed(() => {
  const cardsWithOwned = allCards.value.map(card => {
    const ownedCard = userOwnedCards.value.find(owned => owned.cardId === card.id)
    return {
      ...card,
      owned: ownedCard ? ownedCard.quantity > 0 : false,
      quantity: ownedCard ? (ownedCard.quantity || 0) : 0
    }
  })
  
  if (currentFilter.value === 'all') {
    return cardsWithOwned
  }
  return cardsWithOwned.filter(c => c.rarity === currentFilter.value)
})

const unlockedCount = computed(() => unlockedAchievementCodes.value.length)
const totalAchievements = computed(() => allAchievements.value.length)

function getRarityLabel(rarity) {
  const labels = { 1: '普通', 2: '稀有', 3: '史诗', 4: '传说' }
  return labels[rarity] || '普通'
}

function isAchievementUnlocked(code) {
  return unlockedAchievementCodes.value.includes(code)
}

function showCardDetail(card) {
  selectedCard.value = card
  showCardDialog.value = true
}

function showAchievementDetail(achievement) {
  selectedAchievement.value = achievement
  showAchievementDialog.value = true
}

async function fetchData() {
  try {
    const [cardDefsRes, userCardsRes, achievementsRes, userAchievementsRes] = await Promise.all([
      cardsApi.getDefinitions(),
      cardsApi.getUserCards(),
      achievementsApi.getAll(),
      achievementsApi.getUser()
    ])
    
    allCards.value = cardDefsRes.data.data || []
    userOwnedCards.value = userCardsRes.data.data || []
    
    allAchievements.value = achievementsRes.data.data || []
    unlockedAchievementCodes.value = (userAchievementsRes.data.data || [])
      .map(a => a.code)
  } catch (error) {
    console.error('Failed to fetch collection data:', error)
    ElMessage.error('加载收藏数据失败')
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.collection-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #0B0E14 0%, #0A0D12 100%);
  padding: 20px;
}

.header-section {
  text-align: center;
  margin-bottom: 30px;
}

.header-section h1 {
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, #B8860B, #D4AF37, #F5D061);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 20px;
}

.tab-switcher {
  display: flex;
  background: rgba(30, 41, 59, 0.6);
  border-radius: 16px;
  padding: 6px;
  width: fit-content;
  margin: 0 auto;
  gap: 8px;
}

.tab-btn {
  padding: 12px 32px;
  border: none;
  background: transparent;
  color: #94A3B8;
  font-size: 15px;
  font-weight: 600;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tab-btn.active {
  background: linear-gradient(135deg, #D4AF37, #B8860B);
  color: #FFFFFF;
  box-shadow: 0 4px 15px rgba(212, 175, 55, 0.4);
}

.content-section {
  max-width: 430px;
  margin: 0 auto;
}

.stats-card, .achievement-stats {
  background: linear-gradient(135deg, rgba(30, 41, 59, 0.8), rgba(18, 24, 38, 0.8));
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 20px;
  padding: 24px;
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.stat-icon {
  font-size: 32px;
  margin-right: 12px;
}

.stat-label {
  flex: 1;
  color: #94A3B8;
  font-size: 14px;
}

.stat-value {
  color: #D4AF37;
  font-size: 24px;
  font-weight: 700;
}

.progress-bar {
  height: 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #B8860B, #D4AF37);
  border-radius: 4px;
  transition: width 0.5s ease;
}

.progress-fill.achievement {
  background: linear-gradient(90deg, #60A5FA, #3B82F6);
}

.hint-text {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px 16px;
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.1), rgba(212, 175, 55, 0.05));
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 12px;
  margin-bottom: 16px;
  font-size: 13px;
  color: #D4AF37;
}

.hint-icon {
  margin-right: 8px;
  font-size: 16px;
}

.rarity-filters {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.filter-btn {
  padding: 8px 16px;
  border: 1px solid rgba(212, 175, 55, 0.3);
  background: rgba(30, 41, 59, 0.4);
  color: #94A3B8;
  font-size: 13px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.filter-btn.active {
  background: rgba(212, 175, 55, 0.2);
  border-color: #D4AF37;
  color: #D4AF37;
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.card-item {
  background: linear-gradient(135deg, rgba(30, 41, 59, 0.8), rgba(18, 24, 38, 0.8));
  border-radius: 16px;
  padding: 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  border: 2px solid rgba(148, 163, 184, 0.3);
}

.card-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.3);
}

.card-item.rarity-1 { border-color: rgba(148, 163, 184, 0.5); }
.card-item.rarity-2 { border-color: rgba(96, 165, 250, 0.5); }
.card-item.rarity-3 { border-color: rgba(168, 85, 247, 0.5); }
.card-item.rarity-4 { border-color: rgba(245, 158, 11, 0.5); }

.card-item.locked {
  opacity: 0.5;
}

.card-item.locked .card-emoji {
  filter: blur(3px);
}

.card-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.card-emoji {
  font-size: 48px;
  margin-bottom: 8px;
}

.card-name {
  font-size: 13px;
  color: #E2E8F0;
  font-weight: 600;
}

.card-count {
  font-size: 11px;
  color: #D4AF37;
  margin-top: 4px;
}

.rarity-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 6px;
  background: rgba(0, 0, 0, 0.5);
  color: #94A3B8;
}

.rarity-2 .rarity-badge { color: #60A5FA; }
.rarity-3 .rarity-badge { color: #A855F7; }
.rarity-4 .rarity-badge { color: #F59E0B; }

.achievements-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.achievement-item {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, rgba(30, 41, 59, 0.8), rgba(18, 24, 38, 0.8));
  border: 1px solid rgba(212, 175, 55, 0.15);
  border-radius: 16px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.achievement-item:hover {
  transform: translateX(4px);
  border-color: rgba(212, 175, 55, 0.3);
}

.achievement-item.unlocked {
  border-color: rgba(212, 175, 55, 0.3);
}

.achievement-badge {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.2), rgba(212, 175, 55, 0.1));
  border: 2px solid #D4AF37;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.achievement-badge.locked {
  background: rgba(100, 116, 139, 0.2);
  border-color: #64748B;
}

.badge-icon {
  font-size: 28px;
}

.achievement-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.achievement-name {
  font-size: 16px;
  font-weight: 600;
  color: #E2E8F0;
  margin-bottom: 4px;
}

.achievement-desc {
  font-size: 13px;
  color: #94A3B8;
}

.achievement-status {
  font-size: 24px;
}

.status-unlocked {
  color: #D4AF37;
}

.status-locked {
  opacity: 0.5;
}

.card-detail, .achievement-detail {
  text-align: center;
  padding: 20px;
}

.card-showcase, .achievement-showcase {
  width: 200px;
  height: 200px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, rgba(30, 41, 59, 0.9), rgba(18, 24, 38, 0.9));
  border-radius: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 3px solid;
}

.card-showcase.rarity-1, .achievement-showcase { border-color: #94A3B8; }
.card-showcase.rarity-2 { border-color: #60A5FA; }
.card-showcase.rarity-3 { border-color: #A855F7; }
.card-showcase.rarity-4 { border-color: #F59E0B; }

.showcase-emoji {
  font-size: 80px;
  margin-bottom: 12px;
}

.showcase-icon {
  font-size: 80px;
}

.showcase-rarity {
  font-size: 14px;
  padding: 6px 16px;
  border-radius: 12px;
  background: rgba(0, 0, 0, 0.4);
}

.showcase-status {
  font-size: 14px;
  color: #94A3B8;
  margin-top: 12px;
}

.card-description, .achievement-description {
  margin-bottom: 20px;
}

.card-description p, .achievement-description p {
  color: #94A3B8;
  font-size: 15px;
  line-height: 1.6;
}

.card-meta {
  display: flex;
  justify-content: center;
  gap: 20px;
  color: #64748B;
  font-size: 13px;
}

:deep(.el-dialog) {
  background: linear-gradient(180deg, #1A202C 0%, #0A0D12 100%) !important;
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 20px;
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid rgba(212, 175, 55, 0.1);
}

:deep(.el-dialog__title) {
  color: #D4AF37 !important;
  font-weight: 600;
}

:deep(.el-dialog__body) {
  padding: 20px;
}
</style>
