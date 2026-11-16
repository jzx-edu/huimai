import { createRouter, createWebHashHistory, type RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: () => import('../layouts/TabBarLayout.vue'),
    redirect: '/feed',
    children: [
      {
        path: 'feed',
        name: 'feed',
        component: () => import('../views/FeedView.vue')
      },
      {
        path: 'discover',
        name: 'discover',
        component: () => import('../views/DiscoverView.vue')
      },
      {
        path: 'shop',
        name: 'shop',
        component: () => import('../views/ShopView.vue')
      },
      {
        path: 'messages',
        name: 'messages',
        component: () => import('../views/MessageView.vue')
      },
      {
        path: 'profile',
        name: 'profile',
        component: () => import('../views/ProfileView.vue')
      }
    ]
  },
  {
    path: '/user/:id',
    name: 'user-home',
    component: () => import('../views/UserHomeView.vue')
  },
  {
    path: '/publish',
    name: 'publish',
    component: () => import('../views/PublishView.vue')
  },
  {
    path: '/chat/:id',
    name: 'chat',
    component: () => import('../views/ChatView.vue')
  },
  {
    path: '/notifications',
    name: 'notifications',
    component: () => import('../views/NotificationView.vue')
  },
  {
    path: '/follow/:id',
    name: 'follow-list',
    component: () => import('../views/FollowListView.vue')
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to) => {
  const token = localStorage.getItem('huimai_token')
  if (!to.meta.public && !token) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.name === 'login' && token) {
    return { name: 'feed' }
  }
  return true
})

export default router
