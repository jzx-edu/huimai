import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import type { LoginResult, UserInfo } from '../types'

const TOKEN_KEY = 'huimai_token'
const USER_KEY = 'huimai_user'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem(TOKEN_KEY) || '')
  const userInfo = ref<UserInfo | null>(
    (() => {
      const raw = localStorage.getItem(USER_KEY)
      return raw ? (JSON.parse(raw) as UserInfo) : null
    })()
  )

  const isLoggedIn = computed(() => !!token.value)

  function setLogin(res: LoginResult) {
    token.value = res.token
    const base: UserInfo = {
      id: res.userId,
      username: res.username,
      nickname: res.nickname,
      avatar: res.avatar
    }
    userInfo.value = base
    localStorage.setItem(TOKEN_KEY, res.token)
    localStorage.setItem(USER_KEY, JSON.stringify(base))
  }

  function setUserInfo(info: UserInfo) {
    userInfo.value = info
    localStorage.setItem(USER_KEY, JSON.stringify(info))
  }

  function clear() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  return { token, userInfo, isLoggedIn, setLogin, setUserInfo, clear }
})
