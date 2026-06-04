import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getAdminInfo } from '@/api/admin'

export const useAdminStore = defineStore('admin', () => {
  const adminInfo = ref(null)
  const token = ref(localStorage.getItem('admin_token') || '')

  async function fetchAdminInfo() {
    try {
      const res = await getAdminInfo()
      adminInfo.value = res.data
      return res.data
    } catch (error) {
      return null
    }
  }

  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('admin_token', newToken)
  }

  function setAdminId(id) {
    localStorage.setItem('admin_id', id)
  }

  function logout() {
    token.value = ''
    adminInfo.value = null
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_id')
  }

  return {
    adminInfo,
    token,
    fetchAdminInfo,
    setToken,
    setAdminId,
    logout
  }
})
