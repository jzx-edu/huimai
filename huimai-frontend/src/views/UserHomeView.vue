<template>
  <div class="user-home">
    <div class="nav">
      <van-icon name="arrow-left" @click="back" />
      <span class="nav-title">用户主页</span>
    </div>

    <div class="profile-card">
      <van-image round width="84" height="84" :src="assetUrl(user?.avatar) || defaultAvatar" />
      <div class="nickname">{{ user?.nickname }}</div>
      <div class="bio">{{ user?.bio || '这个人还没有简介' }}</div>

      <div class="stats">
        <div class="stat">
          <b>{{ user?.followingCount ?? 0 }}</b>
          <span>关注</span>
        </div>
        <div class="stat">
          <b>{{ user?.followerCount ?? 0 }}</b>
          <span>粉丝</span>
        </div>
      </div>

      <div class="btn-row">
        <van-button
          round
          :type="followed ? 'default' : 'primary'"
          :icon="followed ? 'success' : 'plus'"
          @click="handleFollow"
        >
          {{ followed ? '已关注' : '关注' }}
        </van-button>
        <van-button round plain icon="chat-o" @click="sendMessage">发私信</van-button>
      </div>
    </div>

    <div class="section-title">全部作品</div>
    <div class="grid no-scrollbar">
      <van-empty v-if="videos.length === 0" description="暂无公开作品" />
      <div v-for="v in videos" :key="v.id" class="grid-item" @click="preview(v)">
        <van-image v-if="v.coverUrl" width="100%" height="100%" fit="cover" :src="assetUrl(v.coverUrl)" />
        <div v-else class="cover-placeholder">
          <van-icon name="video" />
        </div>
        <div class="g-title">{{ v.title }}</div>
      </div>
    </div>

    <van-popup v-model:show="previewShow" teleport="#popup-root" :style="{ width: '100%', height: '100%' }">
      <div class="preview-box">
        <video
          v-if="previewItem"
          :src="assetUrl(previewItem.videoUrl)"
          :poster="assetUrl(previewItem.coverUrl)"
          controls
          autoplay
          playsinline
          class="preview-video"
        />
        <van-icon name="cross" class="close" @click="previewShow = false" />
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showSuccessToast } from 'vant'
import service from '../utils/request'
import { assetUrl } from '../utils/url'
import { toggleFollow } from '../api/modules/interaction'
import { getUserVideos } from '../api/modules/video'
import type { UserInfo, VideoItem } from '../types'

const route = useRoute()
const router = useRouter()

const defaultAvatar =
  'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="84" height="84"><rect width="84" height="84" rx="42" fill="%230e6b52"/><text x="42" y="53" font-size="32" fill="white" text-anchor="middle">%E4%BA%BA</text></svg>'

const userId = Number(route.params.id)
const user = ref<UserInfo | null>(null)
const followed = ref(false)
const videos = ref<VideoItem[]>([])
const previewShow = ref(false)
const previewItem = ref<VideoItem | null>(null)

onMounted(async () => {
  user.value = (await service.get(`/user/profile/${userId}`)) as UserInfo
  followed.value = user.value.followed === true
  const data = await getUserVideos(userId, 1, 30)
  videos.value = data.list
})

function preview(v: VideoItem) {
  previewItem.value = v
  previewShow.value = true
}

async function handleFollow() {
  const res = await toggleFollow(userId)
  followed.value = res.followed
  showSuccessToast(res.followed ? '已关注' : '已取消关注')
}

// 进入与该用户的聊天页
function sendMessage() {
  router.push(`/chat/${userId}`)
}

function back() {
  router.back()
}
</script>

<style scoped>
.user-home {
  height: 100%;
  overflow-y: auto;
  background: #0a0f1a;
}
.nav {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: calc(env(safe-area-inset-top) + 12px) 16px 10px;
  font-size: 20px;
}
.nav-title {
  font-size: 16px;
  font-weight: 600;
}
.profile-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 26px 24px;
  text-align: center;
}
.nickname {
  margin-top: 14px;
  font-size: 20px;
  font-weight: 700;
}
.bio {
  margin-top: 8px;
  color: var(--huimai-muted);
  font-size: 13px;
}
.stats {
  display: flex;
  gap: 38px;
  margin: 22px 0 26px;
}
.stat {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
  color: var(--huimai-muted);
}
.stat b {
  font-size: 19px;
  color: #fff;
}
.btn-row {
  display: flex;
  gap: 12px;
  width: 100%;
}
.btn-row .van-button {
  flex: 1;
}
.section-title {
  padding: 18px 16px 8px;
  font-size: 13px;
  color: var(--huimai-muted);
}
.grid {
  padding: 0 8px 20px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}
.grid-item {
  position: relative;
  aspect-ratio: 9 / 16;
  border-radius: 8px;
  overflow: hidden;
  background: #000;
}
.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg, #1e3a8a, #0a0f1a);
  color: rgba(255, 255, 255, 0.55);
}
.cover-placeholder .van-icon {
  font-size: 34px;
}
.g-title {
  position: absolute;
  left: 6px;
  right: 6px;
  bottom: 6px;
  font-size: 12px;
  color: #fff;
  text-shadow: 0 1px 3px #000;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.preview-box {
  position: relative;
  height: 100%;
  background: #000;
  display: flex;
  align-items: center;
}
.preview-video {
  width: 100%;
  max-height: 100%;
  object-fit: contain;
}
.close {
  position: absolute;
  top: calc(env(safe-area-inset-top) + 12px);
  right: 16px;
  font-size: 26px;
  color: #fff;
}
</style>
