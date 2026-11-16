import { request } from '../../utils/request'
import type { PageData, VideoItem } from '../../types'

export interface PublishParams {
  file: File
  title: string
  description?: string
  categoryId?: number
  isKnowledge?: number
  tags?: string[]
  sources?: string[]
}

export function publishVideo(params: PublishParams) {
  const form = new FormData()
  form.append('file', params.file)
  form.append('title', params.title)
  if (params.description) form.append('description', params.description)
  if (params.categoryId) form.append('categoryId', String(params.categoryId))
  if (params.isKnowledge) form.append('isKnowledge', String(params.isKnowledge))
  params.tags?.forEach((t) => form.append('tags', t))
  params.sources?.forEach((s) => form.append('sources', s))
  return request<VideoItem>({
    url: '/videos/publish',
    method: 'post',
    data: form,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function getFeed(page = 1, size = 10) {
  return request<PageData<VideoItem>>({ url: '/videos/feed', method: 'get', params: { page, size } })
}

export function getVideoDetail(id: number) {
  return request<VideoItem>({ url: `/videos/${id}`, method: 'get' })
}

export function reportPlay(id: number) {
  return request<void>({ url: `/videos/${id}/play`, method: 'post' })
}

export function deleteVideo(id: number) {
  return request({ url: `/videos/${id}`, method: 'delete' })
}

export function getByCategory(categoryId: number, page = 1, size = 10) {
  return request<PageData<VideoItem>>({
    url: `/videos/category/${categoryId}`,
    method: 'get',
    params: { page, size }
  })
}

export function getUserVideos(userId: number, page = 1, size = 10) {
  return request<PageData<VideoItem>>({
    url: `/videos/user/${userId}`,
    method: 'get',
    params: { page, size }
  })
}

export function searchVideos(keyword: string, page = 1, size = 10) {
  return request<PageData<VideoItem>>({
    url: '/videos/search',
    method: 'get',
    params: { keyword, page, size }
  })
}
