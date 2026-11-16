<template>
  <div class="chat-page">
    <div class="nav">
      <van-icon name="arrow-left" @click="back" />
      <span class="nav-title" @click="goUserHome">{{ peerName || '私信' }}</span>
    </div>

    <div ref="bodyRef" class="chat-body no-scrollbar">
      <van-empty
        v-if="!loading && messages.length === 0"
        description="还没有消息，打个招呼吧"
      />
      <div
        v-for="m in messages"
        :key="m.id"
        class="msg-row"
        :class="isMine(m) ? 'mine' : 'peer'"
      >
        <van-image
          v-if="!isMine(m)"
          round
          width="36"
          height="36"
          :src="assetUrl(peerAvatar) || defaultAvatar"
        />
        <div
          class="bubble"
          :class="{ clickable: isMine(m) }"
          @click="isMine(m) ? confirmDelete(m) : undefined"
        >{{ m.content }}</div>
      </div>
    </div>

    <div class="input-bar">
      <van-field
        v-model="text"
        placeholder="说点什么……"
        class="chat-input"
        @keyup.enter="send"
      />
      <van-button type="primary" size="small" :disabled="!text.trim()" @click="send">
        发送
      </van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import service from '../utils/request'
import { deleteMessage, getChat, markRead, sendMessage } from '../api/modules/message'
import { useUserStore } from '../stores/user'
import { usePolling } from '../composables/usePolling'
import { assetUrl } from '../utils/url'
import type { PrivateMessage, UserInfo } from '../types'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const defaultAvatar =
  'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="36" height="36"><rect width="36" height="36" rx="18" fill="%231e3a8a"/><text x="18" y="23" font-size="14" fill="white" text-anchor="middle">%E4%BA%BA</text></svg>'

const peerId = Number(route.params.id)
const myId = userStore.userInfo?.id

const messages = ref<PrivateMessage[]>([])
const text = ref('')
const loading = ref(true)
const bodyRef = ref<HTMLElement | null>(null)
const peerName = ref('')
const peerAvatar = ref('')

onMounted(async () => {
  try {
    const peer = (await service.get(`/user/profile/${peerId}`)) as UserInfo
    peerName.value = peer.nickname
    peerAvatar.value = peer.avatar || ''
    messages.value = await getChat(peerId)
    await markRead(peerId)
    await scrollBottom()
  } catch {
    showToast('加载失败')
  } finally {
    loading.value = false
  }
})

function isMine(m: PrivateMessage) {
  return m.senderId === myId
}

async function send() {
  const content = text.value.trim()
  if (!content) {
    return
  }
  const saved = await sendMessage(peerId, content)
  messages.value.push(saved)
  text.value = ''
  await scrollBottom()
}

async function confirmDelete(m: PrivateMessage) {
  try {
    await showConfirmDialog({
      title: '删除消息',
      message: '确定删除这条消息吗？'
    })
  } catch {
    // 用户取消
    return
  }
  await deleteMessage(m.id)
  messages.value = messages.value.filter((x) => x.id !== m.id)
}

async function scrollBottom() {
  await nextTick()
  if (bodyRef.value) {
    bodyRef.value.scrollTop = bodyRef.value.scrollHeight
  }
}

function isAtBottom() {
  const el = bodyRef.value
  if (!el) {
    return true
  }
  // 距底部 60px 以内视为"在底部"
  return el.scrollHeight - el.scrollTop - el.clientHeight < 60
}

async function refresh() {
  const latest = await getChat(peerId)
  const lastId = messages.value.length
    ? messages.value[messages.value.length - 1].id
    : 0
  const newLastId = latest.length ? latest[latest.length - 1].id : 0
  if (newLastId === lastId) {
    return
  }
  const atBottom = isAtBottom()
  messages.value = latest
  await markRead(peerId)
  // 用户正在看历史消息时不强制滚动，否则跟随到底部
  if (atBottom) {
    await scrollBottom()
  }
}

// 每 3 秒轮询一次新消息
usePolling(refresh, 3000)

function goUserHome() {
  router.push(`/user/${peerId}`)
}

function back() {
  router.back()
}
</script>

<style scoped>
.chat-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--huimai-bg);
}
.nav {
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
.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 12px 14px;
}
.msg-row {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  margin-bottom: 14px;
}
.msg-row.mine {
  justify-content: flex-end;
}
.bubble {
  max-width: 72%;
  padding: 9px 13px;
  border-radius: 14px;
  font-size: 14px;
  line-height: 1.45;
  word-break: break-word;
}
.bubble.clickable {
  cursor: pointer;
}
.msg-row.peer .bubble {
  background: #1c2638;
  color: var(--huimai-text);
  border-bottom-left-radius: 4px;
}
.msg-row.mine .bubble {
  background: var(--huimai-primary);
  color: #fff;
  border-bottom-right-radius: 4px;
}
.input-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px calc(env(safe-area-inset-bottom) + 8px);
  background: #111827;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}
.chat-input {
  flex: 1;
  padding: 0;
  background: transparent;
}
.chat-input :deep(.van-field__control) {
  background: #0a0f1a;
  border-radius: 18px;
  padding: 9px 14px;
  color: var(--huimai-text);
}
.chat-input :deep(.van-field__control::placeholder) {
  color: var(--huimai-muted);
}
</style>
