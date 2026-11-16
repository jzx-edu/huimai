import { request } from '../../utils/request'
import type { NotificationItem } from '../../types'

export function getNotifications() {
  return request<NotificationItem[]>({
    url: '/notifications/list',
    method: 'get'
  })
}

export function getNotificationUnreadCount() {
  return request<number>({
    url: '/notifications/unread-count',
    method: 'get'
  })
}

export function markNotificationRead(id: number) {
  return request({
    url: `/notifications/read/${id}`,
    method: 'put'
  })
}

export function markAllNotificationsRead() {
  return request({
    url: '/notifications/read-all',
    method: 'put'
  })
}
