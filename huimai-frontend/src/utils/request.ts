import axios, { type AxiosRequestConfig } from 'axios'
import { showToast } from 'vant'
import { useUserStore } from '../stores/user'
import router from '../router'
import type { ApiResult } from '../types'

const service = axios.create({
  // 开发环境为 /api（Vite 代理），生产构建为后端绝对地址（原生 App 直连）
  baseURL: (import.meta.env.VITE_API_BASE as string) || '/api',
  timeout: 60000
})

// 请求拦截：附带 token
service.interceptors.request.use((config) => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})

// 响应拦截：拆包统一返回，处理业务错误与 401
service.interceptors.response.use(
  (response) => {
    const body = response.data as ApiResult
    // 非 JSON 结构（如直接返回的文件）直接原样返回
    if (body === null || typeof body !== 'object' || body.code === undefined) {
      return response.data
    }
    if (body.code === 200) {
      return body.data
    }
    if (body.code === 401) {
      handleUnauthorized()
      return Promise.reject(new Error(body.message))
    }
    showToast(body.message || '请求失败')
    return Promise.reject(new Error(body.message))
  },
  (error) => {
    if (error.response?.status === 401) {
      handleUnauthorized()
    } else {
      showToast(error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

function handleUnauthorized() {
  const userStore = useUserStore()
  userStore.clear()
  if (router.currentRoute.value.name !== 'login') {
    router.replace({ name: 'login' })
  }
}

// 以泛型返回真实 data 类型
export function request<T = unknown>(config: AxiosRequestConfig): Promise<T> {
  return service.request(config) as unknown as Promise<T>
}

export default service
