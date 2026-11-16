<template>
  <div class="message-page">
    <div class="topbar">
      <span class="top-title">私信</span>
    </div>

    <div v-if="loading" class="loading">
      <van-loading />
    </div>
    <van-empty v-else-if="list.length === 0" description="还没有私信，去视频里找作者聊聊吧" />

    <div v-else class="conv-list no-scrollbar">
      <div v-for="c in list" :key="c.userId" class="conv-item" @click="openChat(c)">
        <van-image round width="48" height="48" :src="assetUrl(c.avatar) || defaultAvatar" />
        <div class="conv-main">
          <div class="conv-top">
            <span class="conv-name">{{ c.nickname }}</span>
            <span class="conv-time">{{ formatTime(c.lastTime) }}</span>
          </div>
          <div class="conv-bottom">
            <span class="conv-last">{{ c.lastContent }}</span>
            <span v-if="c.unreadCount > 0" class="badge">
              {{ c.unreadCount > 99 ? '99+' : c.unreadCount }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getConversations } from '../api/modules/message'
import { usePolling } from '../composables/usePolling'
import { assetUrl } from '../utils/url'
import type { Conversation } from '../types'

const router = useRouter()

const defaultAvatar =
  'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="48" height="48"><rect width="48" height="48" rx="24" fill="%231e3a8a"/><text x="24" y="31" font-size="18" fill="white" text-anchor="middle">%E4%BA%BA</text></svg>'

const list = ref<Conversation[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    list.value = await getConversations()
  } finally {
    loading.value = false
  }
})

async function refresh() {
  list.value = await getConversations()
}

// 每 5 秒轮询会话列表
usePolling(refresh, 5000)

function openChat(c: Conversation) {
  router.push(`/chat/${c.userId}`)
}

function formatTime(t: string) {
  const d = new Date(t)
  const now = new Date()
  const pad = (n: number) => String(n).padStart(2, '0')
  const sameDay = d.toDateString() === now.toDateString()
  if (sameDay) {
    return `${pad(d.getHours())}:${pad(d.getMinutes())}`
  }
  if (d.getFullYear() === now.getFullYear()) {
    return `${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
  }
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}
</script>

<style scoped>
.message-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--huimai-bg);
}
.topbar {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: calc(env(safe-area-inset-top) + 14px) 16px 12px;
}
.top-title {
  font-size: 17px;
  font-weight: 700;
}
.loading {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
.conv-list {
  flex: 1;
  overflow-y: auto;
  padding-bottom: calc(var(--tabbar-height) + env(safe-area-inset-bottom) + 12px);
}
.conv-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
}
.conv-item:active {
  background: rgba(255, 255, 255, 0.04);
}
.conv-main {
  flex: 1;
  min-width: 0;
}
.conv-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}
.conv-name {
  font-size: 15px;
  font-weight: 600;
}
.conv-time {
  font-size: 12px;
  color: var(--huimai-muted);
  flex-shrink: 0;
}
.conv-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-top: 5px;
}
.conv-last {
  flex: 1;
  min-width: 0;
  font-size: 13px;
  color: var(--huimai-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.badge {
  flex-shrink: 0;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: var(--huimai-primary);
  color: #fff;
  font-size: 11px;
  line-height: 18px;
  text-align: center;
}
</style>
