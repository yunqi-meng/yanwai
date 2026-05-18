import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../api'

export const useUserStore = defineStore('user', () => {
  const userId = ref(localStorage.getItem('userId') || null)
  const nickname = ref('')
  const memberLevel = ref(0)
  const dailyAnalysisCount = ref(0)
  const totalAnalysis = ref(0)
  const totalShare = ref(0)
  const lateNightCount = ref(0)
  const workplaceCount = ref(0)
  const romanceCount = ref(0)
  const memberExpireTime = ref(null)
  const canWatchAd = ref(true)
  const isLoggedIn = computed(() => !!userId.value)

  const remainingAnalysis = computed(() => {
    if (memberLevel.value === 1) return 10
    return 3 - dailyAnalysisCount.value
  })

  async function login() {
    let deviceId = localStorage.getItem('deviceId')
    if (!deviceId) {
      deviceId = 'device_' + Date.now()
      localStorage.setItem('deviceId', deviceId)
    }

    try {
      const res = await api.post('/api/user/login', { openid: deviceId })
      if (res.data.code === 200) {
        const data = res.data.data
        userId.value = data.userId
        nickname.value = data.nickname
        memberLevel.value = data.memberLevel
        dailyAnalysisCount.value = data.dailyAnalysisCount
        totalAnalysis.value = data.totalAnalysis
        totalShare.value = data.totalShare
        memberExpireTime.value = data.memberExpireTime
        canWatchAd.value = data.canWatchAd
        localStorage.setItem('userId', data.userId)
        return true
      }
    } catch (e) {
      console.error('Login failed:', e)
      return false
    }
  }

  async function fetchStats() {
    if (!userId.value) return
    try {
      const res = await api.get('/api/user/stats')
      if (res.data.code === 200) {
        const data = res.data.data
        nickname.value = data.nickname
        memberLevel.value = data.memberLevel
        dailyAnalysisCount.value = data.dailyAnalysisCount
        totalAnalysis.value = data.totalAnalysis
        totalShare.value = data.totalShare
        lateNightCount.value = data.lateNightCount
        workplaceCount.value = data.workplaceCount
        romanceCount.value = data.romanceCount
        memberExpireTime.value = data.memberExpireTime
        canWatchAd.value = data.canWatchAd
      }
    } catch (e) {
      console.error('Fetch stats failed:', e)
    }
  }

  async function resetData() {
    if (!userId.value) return
    try {
      await api.post('/api/user/reset')
      await fetchStats()
    } catch (e) {
      console.error('Reset failed:', e)
    }
  }

  function incrementDailyCount() {
    dailyAnalysisCount.value++
    totalAnalysis.value++
  }

  async function watchAdComplete() {
    if (!userId.value) return false
    try {
      const res = await api.post('/api/user/watch-ad-complete')
      if (res.data.code === 200) {
        await fetchStats()
        return true
      }
      return false
    } catch (e) {
      console.error('Watch ad failed:', e)
      return false
    }
  }

  async function checkCanWatchAd() {
    if (!userId.value) return false
    try {
      const res = await api.get('/api/user/can-watch-ad')
      if (res.data.code === 200) {
        canWatchAd.value = res.data.data.canWatch
        return canWatchAd.value
      }
    } catch (e) {
      console.error('Check can watch ad failed:', e)
    }
    return false
  }

  return {
    userId,
    nickname,
    memberLevel,
    dailyAnalysisCount,
    totalAnalysis,
    totalShare,
    lateNightCount,
    workplaceCount,
    romanceCount,
    memberExpireTime,
    canWatchAd,
    isLoggedIn,
    remainingAnalysis,
    login,
    fetchStats,
    resetData,
    incrementDailyCount,
    watchAdComplete,
    checkCanWatchAd
  }
})
