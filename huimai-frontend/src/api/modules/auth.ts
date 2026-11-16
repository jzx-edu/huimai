import { request } from '../../utils/request'
import type { LoginResult } from '../../types'

export interface LoginPayload {
  username: string
  password: string
}

export interface RegisterPayload {
  username: string
  password: string
  nickname?: string
}

export function login(data: LoginPayload) {
  return request<LoginResult>({ url: '/auth/login', method: 'post', data })
}

export function register(data: RegisterPayload) {
  return request<LoginResult>({ url: '/auth/register', method: 'post', data })
}
