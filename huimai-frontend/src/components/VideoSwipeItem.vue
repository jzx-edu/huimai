<template>
  <div class="swipe-item">
    <video
      ref="videoRef"
      class="video"
      :class="{ landscape: isLandscape }"
      :style="isLandscape ? { aspectRatio: videoRatio } : {}"
      :src="assetUrl(video.videoUrl)"
      :poster="assetUrl(video.coverUrl)"
      loop
      playsinline
      webkit-playsinline
      preload="metadata"
      @click="togglePlay"
      @timeupdate="onTimeUpdate"
      @loadedmetadata="detectOrientation"
    />

    <!-- 暂停遮罩图标 -->
    <div v-if="!playing" class="play-icon" @click="togglePlay">
      <van-icon name="play" />
    </div>

    <!-- 底部播放进度条（紧贴底栏上沿）：仅当前激活视频显示，确保离开信息流时绝不残留 -->
    <div v-show="active" class="progress-bar">
      <div class="progress-played" :style="{ width: progress + '%' }">
        <span class="progress-thumb"></span>
      </div>
    </div>

    <CommentPanel
      :show="commentVisible"
      :video-id="video.id"
      @close="commentVisible = false"
      @changed="onCommentChanged"
    />

    <!-- 视频互动面板：上方评论列表，下方输入框 + 关注/点赞/保存/转发 -->
    <van-popup
      v-model:show="actionPanelVisible"
      teleport="#popup-root"
      position="bottom"
      round
      class="action-popup"
      :style="{ background: '#141a28', height: '62%' }"
    >
      <div class="panel-inner">
        <!-- 作者精简主页：头像、昵称、简介、关注/粉丝数、关注与发私信 -->
        <div class="author-card">
          <div class="ac-top">
            <van-image
              round
              width="54"
              height="54"
              class="ac-avatar"
              :src="assetUrl(authorProfile?.avatar || video.authorAvatar) || defaultAvatar"
              @click="goUserHome"
            />
            <div class="ac-meta" @click="goUserHome">
              <div class="ac-name">{{ authorProfile?.nickname || video.authorName }}</div>
              <div class="ac-bio">{{ authorProfile?.bio || '这个人还没有简介' }}</div>
            </div>
          </div>
          <div class="ac-bottom">
            <div class="ac-actions">
              <van-button
                round
                size="small"
                :type="followed ? 'default' : 'primary'"
                :icon="followed ? 'success' : 'plus'"
                @click="handleFollow"
              >
                {{ followed ? '已关注' : '关注' }}
              </van-button>
              <van-button round size="small" plain icon="chat-o" @click="sendPrivateMessage">
                发私信
              </van-button>
            </div>
          </div>
        </div>

        <!-- 别人的评论列表 -->
        <div class="comment-list no-scrollbar">
          <van-empty
            v-if="!commentLoading && commentList.length === 0"
            description="还没有评论，来抢沙发"
          />
          <van-swipe-cell
            v-for="c in commentList"
            :key="c.id"
            class="c-swipe"
            :disabled="c.userId !== myId"
          >
            <div class="c-item">
              <van-image round width="36" height="36" :src="assetUrl(c.userAvatar) || defaultAvatar" />
              <div class="c-body">
                <div class="c-name">{{ c.userName }}</div>
                <div class="c-content">{{ c.content }}</div>
                <div class="c-foot">
                  <span class="c-time">{{ formatTime(c.createdAt) }}</span>
                  <span class="c-like" :class="{ on: c.liked }" @click="toggleCommentLike(c)">
                    <van-icon :name="c.liked ? 'like' : 'like-o'" />
                    {{ c.likeCount }}
                  </span>
                </div>
              </div>
            </div>
            <template #right>
              <div class="c-swipe-del" @click="removePanelComment(c)">
                <van-icon name="delete-o" />
                <span>删除</span>
              </div>
            </template>
          </van-swipe-cell>
        </div>

        <!-- 底部操作条：输入框 + 互动图标 -->
        <div class="action-bar safe-bottom">
          <div class="input-wrap">
            <van-field
              v-model="commentText"
              placeholder="友善评论，文明表达"
              class="action-input"
              @keyup.enter="submitComment"
            />
          </div>
          <div class="action-icons">
            <span class="action-ico" :class="{ on: liked }" @click="handleLike">
              <svg viewBox="0 0 24 24" class="thumb-icon">
                <path
                  v-if="liked"
                  fill="currentColor"
                  d="M1 21h4V9H1v12zm22-11c0-1.1-.9-2-2-2h-6.31l.95-4.57.03-.32c0-.41-.17-.79-.44-1.06L14.17 1 7.59 7.59C7.22 7.95 7 8.45 7 9v10c0 1.1.9 2 2 2h9c.83 0 1.54-.5 1.84-1.22l3.02-7.05c.09-.23.14-.47.14-.73v-2z"
                />
                <path
                  v-else
                  fill="currentColor"
                  d="M9 21h9c.83 0 1.54-.5 1.84-1.22l3.02-7.05c.09-.23.14-.47.14-.73v-2c0-1.1-.9-2-2-2h-6.31l.95-4.57.03-.32c0-.41-.17-.79-.44-1.06L14.17 1 7.59 7.59C7.22 7.95 7 8.45 7 9v10c0 1.1.9 2 2 2zM9 9l4.58-4.59L12 10h9v2l-3 7H9V9zM1 9h4v12H1z"
                />
              </svg>
            </span>
            <span class="action-ico" :class="{ on: favorited }" @click="handleFavorite">
              <van-icon :name="favorited ? 'star' : 'star-o'" />
            </span>
            <span class="action-ico" @click="handleShare">
              <van-icon name="share-o" />
            </span>
          </div>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { computed, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showSuccessToast } from 'vant'
import CommentPanel from './CommentPanel.vue'
import { useActionStore } from '../stores/action'
import { useUserStore } from '../stores/user'
import { addComment, deleteComment, getComments, likeComment } from '../api/modules/comment'
import { toggleFavorite, toggleFollow, toggleLike } from '../api/modules/interaction'
import service from '../utils/request'
import { assetUrl } from '../utils/url'
import type { CommentItem, ContentSource, UserInfo, VideoItem } from '../types'

const props = defineProps<{ video: VideoItem; active: boolean }>()

const router = useRouter()
const videoRef = ref<HTMLVideoElement>()
const playing = ref(false)
const commentVisible = ref(false)
const progress = ref(0)
// 是否为横屏视频（宽 > 高），由视频元数据动态判断，用于切换显示方式
const isLandscape = ref(false)
// 横屏视频自身宽高比（如 "1280 / 720"），用于让横向卡片精确贴合视频、几乎无黑边
const videoRatio = ref('16 / 9')

// 互动操作面板（由底栏“消息”图标唤起）
const actionStore = useActionStore()
const userStore = useUserStore()
const myId = computed(() => userStore.userInfo?.id)
const actionPanelVisible = ref(false)
const commentText = ref('')
// 面板内的评论列表
const commentList = ref<CommentItem[]>([])
const commentLoading = ref(false)

const defaultAvatar =
  'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="48" height="48"><rect width="48" height="48" fill="%230e6b52"/><text x="24" y="30" font-size="20" fill="white" text-anchor="middle">%E4%BA%BA</text></svg>'

const liked = ref(props.video.liked)
const favorited = ref(props.video.favorited)
const followed = ref(false)
// 作者精简主页信息（头像、昵称、简介、关注/粉丝数），打开面板时拉取
const authorProfile = ref<UserInfo | null>(null)
const likeCount = ref(props.video.likeCount)
const favoriteCount = ref(props.video.favoriteCount)

watch(
  () => props.active,
  (active) => {
    const el = videoRef.value
    if (!el) {
      return
    }
    if (active) {
      el.play()
        .then(() => (playing.value = true))
        .catch(() => (playing.value = false))
    } else {
      el.pause()
      el.currentTime = 0
      playing.value = false
      commentVisible.value = false
      actionPanelVisible.value = false
      progress.value = 0
    }
  }
)

// 底栏“消息”图标唤起：仅当前活跃视频打开面板，并暂停播放
watch(
  () => actionStore.openSignal,
  () => {
    if (!props.active) {
      return
    }
    actionPanelVisible.value = true
    videoRef.value?.pause()
    playing.value = false
    loadPanelComments()
    loadAuthorProfile()
  }
)

// 把当前活跃视频的播放状态同步到 store，供底栏“消息”按钮决定是否点亮
watch(
  [playing, () => props.active],
  () => {
    if (props.active) {
      actionStore.setPlaying(playing.value)
    }
  }
)

// 离开信息流（组件卸载）时重置播放状态，避免按钮残留点亮
onUnmounted(() => {
  actionStore.setPlaying(false)
})

function togglePlay() {
  const el = videoRef.value
  if (!el) {
    return
  }
  if (el.paused) {
    el.play()
    playing.value = true
  } else {
    el.pause()
    playing.value = false
  }
}

function onTimeUpdate() {
  const el = videoRef.value
  if (!el || !el.duration) {
    return
  }
  progress.value = (el.currentTime / el.duration) * 100
  // 兜底：播放时元数据必然可用，防止 loadedmetadata 事件错过导致横竖屏误判
  detectOrientation()
}

// 读取视频真实宽高：判断横竖屏，并记录宽高比供横向卡片贴合使用
function detectOrientation() {
  const el = videoRef.value
  if (el && el.videoWidth && el.videoHeight) {
    isLandscape.value = el.videoWidth > el.videoHeight
    videoRatio.value = `${el.videoWidth} / ${el.videoHeight}`
  }
}

async function handleLike() {
  const res = await toggleLike(props.video.id)
  liked.value = res.liked
  likeCount.value += res.liked ? 1 : -1
}

async function handleFavorite() {
  const res = await toggleFavorite(props.video.id)
  favorited.value = res.favorited
  favoriteCount.value += res.favorited ? 1 : -1
}

async function handleFollow() {
  const res = await toggleFollow(props.video.authorId)
  followed.value = res.followed
  showSuccessToast(res.followed ? '已关注' : '已取消关注')
}

// 打开面板时拉取作者完整资料（含简介、关注/粉丝数与真实关注状态）
async function loadAuthorProfile() {
  const profile = (await service.get(`/user/profile/${props.video.authorId}`)) as UserInfo
  authorProfile.value = profile
  followed.value = profile.followed === true
}

// 进入与该视频作者的聊天页：先关闭面板，再延迟跳转，避免弹层卸载与新页面挂载冲突
function sendPrivateMessage() {
  actionPanelVisible.value = false
  const authorId = props.video.authorId
  setTimeout(() => {
    router.push(`/chat/${authorId}`)
  }, 180)
}

function openComments() {
  commentVisible.value = true
  videoRef.value?.pause()
  playing.value = false
}

function onCommentChanged() {
  props.video.commentCount += 1
}

// 直接在操作面板的输入框提交评论，提交后刷新评论列表
async function submitComment() {
  const content = commentText.value.trim()
  if (!content) {
    return
  }
  await addComment(props.video.id, content)
  commentText.value = ''
  props.video.commentCount += 1
  showSuccessToast('评论成功')
  await loadPanelComments()
}

// 加载该视频的评论列表
async function loadPanelComments() {
  commentLoading.value = true
  try {
    const data = await getComments(props.video.id, 1, 50)
    commentList.value = data.list
  } finally {
    commentLoading.value = false
  }
}

async function removePanelComment(c: CommentItem) {
  try {
    await showConfirmDialog({
      title: '删除评论',
      message: '确定删除这条评论吗？'
    })
  } catch {
    // 用户取消
    return
  }
  await deleteComment(c.id)
  props.video.commentCount -= 1
  await loadPanelComments()
}

async function toggleCommentLike(c: CommentItem) {
  const { liked } = await likeComment(c.id)
  c.liked = liked
  c.likeCount += liked ? 1 : -1
}

// 评论时间格式化
function formatTime(t: string) {
  return t ? t.replace('T', ' ').slice(0, 16) : ''
}

function goUserHome() {
  // 先关闭 teleport 到 #app 的弹层，再延迟跳转，避免弹层卸载与新页面挂载冲突导致空白
  actionPanelVisible.value = false
  commentVisible.value = false
  const authorId = props.video.authorId
  setTimeout(() => {
    router.push({ name: 'user-home', params: { id: authorId } })
  }, 180)
}

function handleShare() {
  const url = `${window.location.origin}/#/feed?video=${props.video.id}`
  navigator.clipboard?.writeText(url).then(
    () => showSuccessToast('链接已复制，可分享给朋友'),
    () => showSuccessToast('请手动复制地址栏链接')
  )
}

function sourceLabel(t: string) {
  if (t === 'quran') {
    return '古兰经出处'
  }
  if (t === 'hadith') {
    return '圣训出处'
  }
  return '参考来源'
}
</script>

<style scoped>
.swipe-item {
  position: relative;
  width: 100%;
  height: 100%;
  background: #000;
  overflow: hidden;
}
.video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
/* 横屏视频：在竖屏信息流中以“横向播放卡片”展示。
   宽度铺满整个容器、左右不留空，高度由内联 aspect-ratio 按视频比例决定，垂直居中；
   直角、视频铺满卡片、几乎无黑边，上下仍可滑动切换。 */
.video.landscape {
  position: absolute;
  left: 0;
  top: 50%;
  width: 100%;
  height: auto;
  transform: translateY(-50%);
  object-fit: cover;
  border-radius: 0;
  background: #000;
}
.play-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  color: #fff;
}
.progress-bar {
  position: absolute;
  left: 0;
  right: 0;
  /* 上移到悬浮底栏的上沿：底栏高度 + 真机底部安全区 */
  bottom: calc(var(--tabbar-height) + constant(safe-area-inset-bottom));
  bottom: calc(var(--tabbar-height) + env(safe-area-inset-bottom));
  height: 2px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.18);
  z-index: 22;
  pointer-events: none;
}
.progress-played {
  position: relative;
  height: 100%;
  background: #ffffff;
  border-radius: 999px;
}
.progress-thumb {
  position: absolute;
  right: -4px;
  top: 50%;
  transform: translateY(-50%);
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ffffff;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.4);
}

/* 一体化面板：标题 + 评论列表 + 底部操作条 */
.panel-inner {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.panel-header {
  flex-shrink: 0;
  padding: 13px 14px;
  text-align: center;
  font-size: 14px;
  font-weight: 600;
  color: #eef2f9;
  border-bottom: 1px solid #232c3d;
}
/* 作者精简主页卡片 */
.author-card {
  flex-shrink: 0;
  padding: 14px 16px 16px;
  border-bottom: 1px solid #232c3d;
  background: linear-gradient(180deg, rgba(37, 99, 235, 0.1), rgba(37, 99, 235, 0));
}
.ac-top {
  display: flex;
  align-items: center;
  gap: 12px;
}
.ac-avatar {
  flex-shrink: 0;
  border: 1.5px solid rgba(255, 255, 255, 0.18);
  box-sizing: border-box;
}
.ac-meta {
  flex: 1;
  min-width: 0;
  cursor: pointer;
}
.ac-name {
  font-size: 15px;
  font-weight: 700;
  color: #eef2f9;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.ac-bio {
  margin-top: 3px;
  font-size: 12px;
  line-height: 1.4;
  color: var(--huimai-muted);
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.ac-bottom {
  margin-top: 14px;
}
.ac-actions {
  display: flex;
  gap: 10px;
}
.ac-actions .van-button {
  flex: 1;
  height: 36px;
  font-weight: 600;
}
.comment-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 14px;
}
.c-item {
  display: flex;
  gap: 10px;
  padding: 11px 0;
}
.c-body {
  flex: 1;
  min-width: 0;
}
.c-name {
  font-size: 13px;
  color: var(--huimai-muted);
}
.c-content {
  margin: 3px 0;
  font-size: 15px;
  line-height: 1.4;
  color: #eef2f9;
  word-break: break-word;
}
.c-foot {
  display: flex;
  align-items: center;
  gap: 14px;
}
.c-time {
  font-size: 12px;
  color: var(--huimai-muted);
}
.c-like {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--huimai-muted);
}
.c-like.on {
  color: #e8534a;
}

/* 左滑删除：评论项外层为 SwipeCell，右侧露出红色删除按钮 */
.c-swipe {
  width: 100%;
}
.c-swipe-del {
  width: 76px;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 5px;
  background: #e8534a;
  color: #fff;
  font-size: 12px;
}
.c-swipe-del .van-icon {
  font-size: 20px;
}

/* 底部操作条：输入框在左、图标在右，同一行 */
.action-bar {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-top: 1px solid #232c3d;
}
.input-wrap {
  flex: 1;
  min-width: 0;
}
.action-input {
  background: #1c2434;
  border-radius: 18px;
  padding: 3px 14px;
}
.action-input :deep(.van-field__control) {
  color: #eef2f9;
  font-size: 14px;
}
.action-icons {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 6px;
}
.action-ico {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 40px;
  height: 38px;
  color: #c3ccdb;
  cursor: pointer;
}
.action-ico .van-icon {
  font-size: 22px;
}
.thumb-icon {
  width: 22px;
  height: 22px;
}
.action-ico.on {
  color: var(--huimai-primary);
}
</style>
