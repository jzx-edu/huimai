import { request } from '../../utils/request'
import type { Product, ProductOrder } from '../../types'

/** 商品列表（可按分类、关键词过滤） */
export function getProducts(params?: { category?: string; keyword?: string }) {
  return request<Product[]>({
    url: '/products',
    method: 'get',
    params
  })
}

/** 商品详情 */
export function getProduct(id: number) {
  return request<Product>({
    url: `/products/${id}`,
    method: 'get'
  })
}

/** 下单 */
export function createOrder(data: { productId: number; quantity: number }) {
  return request<ProductOrder>({
    url: '/products/order',
    method: 'post',
    data
  })
}

/** 我的订单 */
export function getMyOrders() {
  return request<ProductOrder[]>({
    url: '/products/orders',
    method: 'get'
  })
}
