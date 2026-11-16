// 接口统一返回结构
export interface ApiResult<T = unknown> {
  code: number
  message: string
  data: T
}

// 分页结构
export interface PageData<T> {
  list: T[]
  total: number
  page: number
  size: number
}

// 用户
export interface UserInfo {
  id: number
  username: string
  nickname: string
  avatar?: string
  bio?: string
  gender?: number
  followerCount?: number
  followingCount?: number
  videoCount?: number
  followed?: boolean
}

// 登录返回
export interface LoginResult {
  token: string
  userId: number
  username: string
  nickname: string
  avatar?: string
}

// 分类
export interface CategoryItem {
  id: number
  name: string
  nameAr?: string
  code: string
  icon?: string
  sort: number
}

// 内容来源
export interface ContentSource {
  id: number
  sourceType: string
  reference: string
  titleAr?: string
  note?: string
}

// 视频
export interface VideoItem {
  id: number
  title: string
  description?: string
  videoUrl: string
  coverUrl?: string
  duration?: number
  width?: number
  height?: number
  isKnowledge?: number
  status?: number
  playCount: number
  likeCount: number
  commentCount: number
  favoriteCount: number
  shareCount?: number
  authorId: number
  authorName: string
  authorAvatar?: string
  categoryId?: number
  categoryName?: string
  liked: boolean
  favorited: boolean
  sources?: ContentSource[]
  createdAt: string
}

// 评论
export interface CommentItem {
  id: number
  videoId: number
  userId: number
  userName: string
  userAvatar?: string
  parentId: number
  content: string
  likeCount: number
  liked?: boolean
  replyCount: number
  createdAt: string
  replies?: CommentItem[]
}

// 私信消息
export interface PrivateMessage {
  id: number
  senderId: number
  receiverId: number
  content: string
  isRead: number
  createdAt: string
}

// 私信会话（按对方用户聚合）
export interface Conversation {
  userId: number
  nickname: string
  avatar?: string
  lastContent: string
  lastTime: string
  unreadCount: number
}

// 通知
export interface NotificationItem {
  id: number
  type: string
  title: string
  content?: string
  relatedId?: number
  isRead: number
  createdAt: string
  fromUserId?: number
  fromUserName?: string
  fromUserAvatar?: string
}

// 商品
export interface Product {
  id: number
  name: string
  category: string
  price: number
  coverImage?: string
  icon?: string
  gradient?: string
  description?: string
  stock: number
  soldCount: number
  createdAt: string
}

// 商品订单（含商品快照）
export interface ProductOrder {
  id: number
  orderNo: string
  userId: number
  productId: number
  quantity: number
  totalAmount: number
  status: number
  createdAt: string
  productName?: string
  productCoverImage?: string
  productIcon?: string
  productGradient?: string
  productPrice?: number
}
