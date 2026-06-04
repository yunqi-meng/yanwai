import { defineStore } from 'pinia'
import { ref } from 'vue'
import { analysisApi } from '../api'

export const useAnalysisStore = defineStore('analysis', () => {
  const isAnalyzing = ref(false)
  const loadingProgress = ref(0)
  const analysisType = ref(null)
  const originalImage = ref(null)
  const analysisText = ref('')
  const error = ref(null)

  const result = ref(null)
  const newCard = ref(null)
  const newAchievements = ref([])

  const isImageAnalyzing = ref(false)

  let progressInterval = null

  function startProgress() {
    stopProgress()
    loadingProgress.value = 0
    progressInterval = setInterval(() => {
      const increment = Math.random() * 10 + 2
      if (loadingProgress.value < 70) {
        loadingProgress.value += increment
      } else if (loadingProgress.value < 90) {
        loadingProgress.value += Math.random() * 3
      }
      if (loadingProgress.value > 90) loadingProgress.value = 90
    }, 400)
  }

  function stopProgress() {
    if (progressInterval) {
      clearInterval(progressInterval)
      progressInterval = null
    }
  }

  function completeProgress() {
    stopProgress()
    loadingProgress.value = 100
  }

  async function startTextAnalysis(text) {
    analysisText.value = text
    analysisType.value = 'text'
    originalImage.value = null
    error.value = null
    result.value = null
    newCard.value = null
    newAchievements.value = []

    isAnalyzing.value = true
    startProgress()

    try {
      const res = await analysisApi.decode(text)
      const data = res.data.data

      completeProgress()

      result.value = data.analysis
      newCard.value = data.newCard
      newAchievements.value = data.newAchievements || []

      analysisText.value = text

      isAnalyzing.value = false

      await new Promise(resolve => setTimeout(resolve, 300))
      return true
    } catch (e) {
      stopProgress()
      error.value = e.message || '解码失败，请重试'
      isAnalyzing.value = false
      throw e
    }
  }

  async function startImageAnalysis(file) {
    analysisType.value = 'image'
    error.value = null
    result.value = null
    newCard.value = null
    newAchievements.value = []

    const reader = new FileReader()
    const imageBase64 = await new Promise((resolve) => {
      reader.onload = (e) => resolve(e.target.result)
      reader.readAsDataURL(file)
    })

    originalImage.value = imageBase64
    isImageAnalyzing.value = true
    isAnalyzing.value = true
    startProgress()

    try {
      const formData = new FormData()
      formData.append('image', file)

      const res = await analysisApi.analyzeImage(formData)
      const data = res.data.data

      completeProgress()

      result.value = data.analysis
      newCard.value = data.newCard
      newAchievements.value = data.newAchievements || []

      analysisText.value = '图片分析'

      isAnalyzing.value = false
      isImageAnalyzing.value = false

      await new Promise(resolve => setTimeout(resolve, 300))
      return true
    } catch (e) {
      stopProgress()
      error.value = e.message || '图片分析失败，请重试'
      isAnalyzing.value = false
      isImageAnalyzing.value = false
      throw e
    }
  }

  function consumeResult() {
    const data = {
      result: result.value,
      newCard: newCard.value,
      newAchievements: newAchievements.value,
      originalImage: originalImage.value,
      analysisText: analysisText.value
    }
    result.value = null
    newCard.value = null
    newAchievements.value = []
    originalImage.value = null
    analysisText.value = ''
    loadingProgress.value = 0
    return data
  }

  function reset() {
    stopProgress()
    isAnalyzing.value = false
    loadingProgress.value = 0
    analysisType.value = null
    originalImage.value = null
    analysisText.value = ''
    error.value = null
    result.value = null
    newCard.value = null
    newAchievements.value = []
    isImageAnalyzing.value = false
  }

  return {
    isAnalyzing,
    loadingProgress,
    analysisType,
    originalImage,
    analysisText,
    error,
    result,
    newCard,
    newAchievements,
    isImageAnalyzing,
    startTextAnalysis,
    startImageAnalysis,
    consumeResult,
    completeProgress,
    reset
  }
})
