import { request } from '../../utils/request'
import type { UserInfo } from '../../types'

export function getFollowingList(userId: number) {
  return request<UserInfo[]>({
    url: `/user/following/${userId}`,
    method: 'get'
  })
}

export function getFollowersList(userId: number) {
  return request<UserInfo[]>({
    url: `/user/followers/${userId}`,
    method: 'get'
  })
}

export function searchUsers(keyword: string) {
  return request<UserInfo[]>({
    url: '/user/search',
    method: 'get',
    params: { keyword }
  })
}
