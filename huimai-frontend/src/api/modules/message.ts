import { request } from '../../utils/request'
import type { Conversation, PrivateMessage } from '../../types'

export function sendMessage(receiverId: number, content: string) {
  return request<PrivateMessage>({
    url: '/messages/send',
    method: 'post',
    data: { receiverId, content }
  })
}

export function getConversations() {
  return request<Conversation[]>({
    url: '/messages/conversations',
    method: 'get'
  })
}

export function getChat(userId: number) {
  return request<PrivateMessage[]>({
    url: `/messages/chat/${userId}`,
    method: 'get'
  })
}

export function markRead(userId: number) {
  return request({
    url: `/messages/read/${userId}`,
    method: 'put'
  })
}

export function getUnreadCount() {
  return request<number>({
    url: '/messages/unread-count',
    method: 'get'
  })
}

export function deleteMessage(id: number) {
  return request({
    url: `/messages/${id}`,
    method: 'delete'
  })
}
