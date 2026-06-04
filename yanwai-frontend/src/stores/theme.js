import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const theme = ref(localStorage.getItem('theme') || 'dark')

  function toggleTheme() {
    theme.value = theme.value === 'dark' ? 'light' : 'dark'
    applyTheme(theme.value)
    localStorage.setItem('theme', theme.value)
  }

  function applyTheme(newTheme) {
    if (newTheme === 'light') {
      document.body.classList.add('theme-light')
    } else {
      document.body.classList.remove('theme-light')
    }
  }

  watch(theme, (newTheme) => {
    applyTheme(newTheme)
  }, { immediate: true })

  return {
    theme,
    toggleTheme
  }
})