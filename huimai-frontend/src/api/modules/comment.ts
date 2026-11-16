import { request } from '../../utils/request'
import type { PageData, CommentItem } from '../../types'

export function addComment(videoId: number, content: string, parentId = 0) {
  return request<CommentItem>({
    url: `/comments/${videoId}`,
    method: 'post',
    data: { content, parentId }
  })
}

export function getComments(videoId: number, page = 1, size = 20) {
  return request<PageData<CommentItem>>({
    url: `/comments/list/${videoId}`,
    method: 'get',
    params: { page, size }
  })
}

export function deleteComment(id: number) {
  return request({
    url: `/comments/${id}`,
    method: 'delete'
  })
}

export function likeComment(id: number) {
  return request<{ liked: boolean }>({
    url: `/comments/like/${id}`,
    method: 'post'
  })
}
