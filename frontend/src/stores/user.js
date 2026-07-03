import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as apiLogin, logout as apiLogout, getUserInfo } from '@/api/auth'
import { getToken, setToken, removeToken } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken() || '')
  const userInfo = ref({})
  const roles = ref([])
  const permissions = ref([])

  async function login(payload) {
    const data = await apiLogin(payload)
    token.value = data.token
    setToken(data.token)
    userInfo.value = data.userInfo || {}
    roles.value = data.userInfo?.roles || []
    permissions.value = data.userInfo?.permissions || []
    return data
  }

  async function fetchUserInfo() {
    const data = await getUserInfo()
    userInfo.value = data
    roles.value = data.roles || []
    permissions.value = data.permissions || []
    return data
  }

  function hasPermission(code) {
    if (!code) return true
    if (permissions.value.includes('*')) return true
    return permissions.value.includes(code)
  }

  async function logout() {
    try { await apiLogout() } catch (e) { /* ignore */ }
    reset()
  }

  function reset() {
    token.value = ''
    userInfo.value = {}
    roles.value = []
    permissions.value = []
    removeToken()
  }

  return { token, userInfo, roles, permissions, login, fetchUserInfo, hasPermission, logout, reset }
})
