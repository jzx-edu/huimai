<template>
  <div class="notif-page">
    <div class="nav">
      <van-icon name="arrow-left" @click="back" />
      <span class="nav-title">通知</span>
      <span v-if="list.length" class="read-all" @click="readAll">全部已读</span>
    </div>

    <van-empty v-if="!loading && list.length === 0" description="还没有通知" />

    <div v-else class="notif-list no-scrollbar">
      <div
        v-for="n in list"
        :key="n.id"
        class="notif-item"
        :class="{ unread: n.isRead === 0 }"
        @click="openItem(n)"
      >
        <van-image round width="46" height="46" :src="assetUrl(n.fromUserAvatar) || defaultAvatar" />
        <div class="n-main">
          <div class="n-line">
            <span class="n-name">{{ n.fromUserName || '系统' }}</span>
            <span class="n-action">{{ n.title }}</span>
          </div>
          <div v-if="n.content" class="n-content">{{ n.content }}</div>
          <div class="n-time">{{ formatTime(n.createdAt) }}</div>
        </div>
        <span v-if="n.isRead === 0" class="dot"></span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getNotifications, markAllNotificationsRead, markNotificationRead } from '../api/modules/notification'
import { assetUrl } from '../utils/url'
import type { NotificationItem } from '../types'

const router = useRouter()

const defaultAvatar =
  'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="46" height="46"><rect width="46" height="46" rx="23" fill="%231e3a8a"/><text x="23" y="30" font-size="18" fill="white" text-anchor="middle">%E4%BA%BA</text></svg>'

const list = ref<NotificationItem[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    list.value = await getNotifications()
  } finally {
    loading.value = false
  }
})

async function openItem(n: NotificationItem) {
  if (n.isRead === 0) {
    await markNotificationRead(n.id)
    n.isRead = 1
  }
  const from = n.fromUserId
  if (n.type === 'message') {
    router.push(`/chat/${from}`)
  } else {
    router.push(`/user/${from}`)
  }
}

async function readAll() {
  await markAllNotificationsRead()
  list.value.forEach((n) => (n.isRead = 1))
}

function formatTime(t: string) {
  const d = new Date(t)
  const now = new Date()
  const pad = (x: number) => String(x).padStart(2, '0')
  if (d.toDateString() === now.toDateString()) {
    return `${pad(d.getHours())}:${pad(d.getMinutes())}`
  }
  if (d.getFullYear() === now.getFullYear()) {
    return `${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
  }
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

function back() {
  router.back()
}
</script>

<style scoped>
.notif-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--huimai-bg);
}
.nav {
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: calc(env(safe-area-inset-top) + 12px) 16px 10px;
  font-size: 20px;
}
.nav-title {
  font-size: 16px;
  font-weight: 600;
}
.read-all {
  position: absolute;
  right: 16px;
  font-size: 13px;
  color: var(--huimai-primary);
}
.notif-list {
  flex: 1;
  overflow-y: auto;
  padding-bottom: calc(var(--tabbar-height) + env(safe-area-inset-bottom) + 12px);
}
.notif-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 13px 16px;
}
.notif-item:active {
  background: rgba(255, 255, 255, 0.04);
}
.n-main {
  flex: 1;
  min-width: 0;
}
.n-line {
  font-size: 14px;
}
.n-name {
  font-weight: 700;
  margin-right: 6px;
}
.n-action {
  color: var(--huimai-muted);
}
.n-content {
  margin-top: 4px;
  font-size: 13px;
  color: var(--huimai-text);
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.n-time {
  margin-top: 5px;
  font-size: 12px;
  color: var(--huimai-muted);
}
.dot {
  flex-shrink: 0;
  width: 9px;
  height: 9px;
  margin-top: 16px;
  border-radius: 50%;
  background: var(--huimai-primary);
}
</style>
