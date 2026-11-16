import { request } from '../../utils/request'

export function toggleLike(videoId: number) {
  return request<{ liked: boolean }>({
    url: `/interaction/like/${videoId}`,
    method: 'post'
  })
}

export function toggleFavorite(videoId: number) {
  return request<{ favorited: boolean }>({
    url: `/interaction/favorite/${videoId}`,
    method: 'post'
  })
}

export function toggleFollow(userId: number) {
  return request<{ followed: boolean }>({
    url: `/interaction/follow/${userId}`,
    method: 'post'
  })
}
