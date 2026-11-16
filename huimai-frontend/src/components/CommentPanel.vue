<template>
  <van-popup
    :show="show"
    teleport="#popup-root"
    position="bottom"
    round
    :style="{ height: '68%' }"
    @close="handleClose"
  >
    <div class="comment-panel">
      <div class="header">{{ total }} 条评论</div>

      <div class="list no-scrollbar" ref="listRef">
        <van-empty v-if="!loading && comments.length === 0" description="还没有评论，来抢沙发" />
        <van-pull-refresh v-model="refreshing" @refresh="loadComments(1)">
          <div v-for="item in comments" :key="item.id" class="comment-item">
            <van-image round width="36" height="36" :src="assetUrl(item.userAvatar) || defaultAvatar" />
            <div class="body">
              <div class="name">{{ item.userName }}</div>
              <div class="content">{{ item.content }}</div>
              <div class="meta">
                <span>{{ formatTime(item.createdAt) }}</span>
                <span class="reply" @click="replyTo(item)">回复</span>
                <span v-if="item.userId === myId" class="del" @click="remove(item)">删除</span>
              </div>
              <div v-for="r in item.replies" :key="r.id" class="reply-item">
                <span class="r-name">{{ r.userName }}：</span>{{ r.content }}
                <van-icon
                  v-if="r.userId === myId"
                  name="delete-o"
                  class="r-del"
                  @click="remove(r)"
                />
              </div>
            </div>
          </div>
        </van-pull-refresh>
      </div>

      <div class="input-bar safe-bottom">
        <van-field
          v-model="text"
          :placeholder="placeholder"
          class="input"
          @keyup.enter="submit"
        />
        <van-button size="small" type="primary" :loading="submitting" @click="submit">
          发送
        </van-button>
      </div>
    </div>
  </van-popup>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { showConfirmDialog, showSuccessToast } from 'vant'
import { addComment, deleteComment, getComments } from '../api/modules/comment'
import { useUserStore } from '../stores/user'
import { assetUrl } from '../utils/url'
import type { CommentItem } from '../types'

const props = defineProps<{ show: boolean; videoId: number }>()
const emit = defineEmits<{
  (e: 'close'): void
  (e: 'changed'): void
}>()

const defaultAvatar =
  'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="48" height="48"><rect width="48" height="48" fill="%230e6b52"/><text x="24" y="30" font-size="20" fill="white" text-anchor="middle">%E4%BA%BA</text></svg>'

const comments = ref<CommentItem[]>([])
const loading = ref(false)
const refreshing = ref(false)
const text = ref('')
const submitting = ref(false)
const replyParent = ref<CommentItem | null>(null)
const listRef = ref<HTMLElement>()

const userStore = useUserStore()
const myId = computed(() => userStore.userInfo?.id)

const total = computed(() => comments.value.length)
const placeholder = computed(() =>
  replyParent.value ? `回复 ${replyParent.value.userName}` : '友善评论，文明表达'
)

watch(
  () => props.show,
  (val) => {
    if (val) {
      replyParent.value = null
      loadComments(1)
    }
  }
)

async function loadComments(page: number) {
  loading.value = true
  try {
    const data = await getComments(props.videoId, page, 50)
    comments.value = data.list
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function replyTo(item: CommentItem) {
  replyParent.value = item
}

async function remove(item: CommentItem) {
  try {
    await showConfirmDialog({
      title: '删除评论',
      message: '确定删除这条评论吗？'
    })
  } catch {
    // 用户取消，不做处理
    return
  }
  await deleteComment(item.id)
  showSuccessToast('已删除')
  await loadComments(1)
  emit('changed')
}

async function submit() {
  if (!text.value.trim()) {
    return
  }
  submitting.value = true
  try {
    await addComment(props.videoId, text.value.trim(), replyParent.value?.id || 0)
    text.value = ''
    replyParent.value = null
    showSuccessToast('评论成功')
    await loadComments(1)
    emit('changed')
  } finally {
    submitting.value = false
  }
}

function handleClose() {
  emit('close')
}

function formatTime(t: string) {
  return t ? t.replace('T', ' ').slice(0, 16) : ''
}
</script>

<style scoped>
.comment-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #141a28;
}
.header {
  padding: 14px;
  text-align: center;
  font-weight: 600;
  border-bottom: 1px solid #232c3d;
}
.list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 14px;
}
.comment-item {
  display: flex;
  gap: 10px;
  padding: 12px 0;
}
.body {
  flex: 1;
  min-width: 0;
}
.name {
  color: var(--huimai-muted);
  font-size: 13px;
}
.content {
  margin: 4px 0;
  font-size: 15px;
  line-height: 1.4;
}
.meta {
  display: flex;
  gap: 16px;
  color: var(--huimai-muted);
  font-size: 12px;
}
.reply {
  color: var(--huimai-gold);
}
.del {
  color: #e8534a;
}
.r-del {
  margin-left: 6px;
  font-size: 13px;
  color: #e8534a;
}
.reply-item {
  margin-top: 6px;
  padding: 6px 8px;
  background: #1c2434;
  border-radius: 6px;
  font-size: 13px;
}
.r-name {
  color: var(--huimai-gold);
}
.input-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-top: 1px solid #232c3d;
  background: #141a28;
}
.input {
  flex: 1;
  background: #1c2434;
  border-radius: 18px;
}
</style>
