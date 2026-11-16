import { request } from '../../utils/request'
import type { CategoryItem } from '../../types'

export function getCategories() {
  return request<CategoryItem[]>({ url: '/categories', method: 'get' })
}
