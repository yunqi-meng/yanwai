import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { userApi } from '../api'

export const useUserStore = defineStore('user', () => {
  const userId = ref(localStorage.getItem('userId') ? Number(localStorage.getItem('userId')) : null)
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
    if (memberLevel.value === 1) {
      return 10 - (dailyAnalysisCount.value || 0)
    }
    return Math.max(0, 3 - (dailyAnalysisCount.value || 0))
  })

  async function login(email, password) {
    try {
      const res = await userApi.login(email, password)
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
    } catch (e) {
      const message = e.response?.data?.message || e.message || '登录失败，请稍后重试'
      throw new Error(message)
    }
  }

  async function register(email, password) {
    try {
      const res = await userApi.register(email, password)
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
    } catch (e) {
      const message = e.response?.data?.message || e.message || '注册失败，请稍后重试'
      throw new Error(message)
    }
  }

  async function guestLogin() {
    let deviceId = localStorage.getItem('deviceId')
    if (!deviceId) {
      deviceId = 'device_' + Date.now()
      localStorage.setItem('deviceId', deviceId)
    }
    
    try {
      const res = await userApi.guestLogin(deviceId)
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
    } catch (e) {
      throw new Error(e.message || '访客登录失败，请稍后重试')
    }
  }

  async function fetchStats() {
    if (!userId.value) return
    try {
      const res = await userApi.getStats()
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
    } catch (e) {
      console.error('Fetch stats failed:', e)
    }
  }

  async function resetData() {
    if (!userId.value) return
    try {
      await userApi.reset()
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
      await userApi.watchAdComplete()
      await fetchStats()
      return true
    } catch (e) {
      console.error('Watch ad failed:', e)
      return false
    }
  }

  async function checkCanWatchAd() {
    if (!userId.value) return false
    try {
      const res = await userApi.canWatchAd()
      canWatchAd.value = res.data.data.canWatch
      return canWatchAd.value
    } catch (e) {
      console.error('Check can watch ad failed:', e)
    }
    return false
  }

  async function updateUserInfo(newNickname) {
    if (!userId.value) return false
    try {
      await userApi.updateInfo(newNickname)
      nickname.value = newNickname
      return true
    } catch (e) {
      console.error('Update user info failed:', e)
      return false
    }
  }

  function logout() {
    userId.value = null
    nickname.value = ''
    memberLevel.value = 0
    dailyAnalysisCount.value = 0
    totalAnalysis.value = 0
    totalShare.value = 0
    lateNightCount.value = 0
    workplaceCount.value = 0
    romanceCount.value = 0
    memberExpireTime.value = null
    canWatchAd.value = true
    localStorage.removeItem('userId')
    localStorage.removeItem('deviceId')
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
    register,
    guestLogin,
    logout,
    fetchStats,
    resetData,
    incrementDailyCount,
    watchAdComplete,
    checkCanWatchAd,
    updateUserInfo
  }
})
