<template>
  <div class="profile">
    <div class="header safe-bottom">
      <van-image round width="68" height="68" :src="assetUrl(user?.avatar) || defaultAvatar" />
      <div class="meta">
        <div class="nickname">{{ user?.nickname }}</div>
        <div class="bio">{{ user?.bio || '这个人还没有简介' }}</div>
        <div class="stats">
          <span class="stat-link" @click="goFollow('following')"><b>{{ user?.followingCount ?? 0 }}</b> 关注</span>
          <span class="stat-link" @click="goFollow('followers')"><b>{{ user?.followerCount ?? 0 }}</b> 粉丝</span>
          <span><b>{{ user?.videoCount ?? 0 }}</b> 作品</span>
        </div>
      </div>
    </div>

    <div class="publish-entry">
      <van-button block round type="primary" icon="plus" @click="goPublish">发布作品</van-button>
    </div>

    <div class="actions-row">
      <div class="notif-wrap">
        <van-button plain size="small" icon="bell" @click="goNotifications">通知</van-button>
        <span v-if="unreadCount > 0" class="notif-badge">
          {{ unreadCount > 99 ? '99+' : unreadCount }}
        </span>
      </div>
      <van-button plain size="small" icon="edit" @click="openEdit">编辑资料</van-button>
      <van-button plain size="small" icon="cross" @click="logout">退出登录</van-button>
    </div>

    <div class="section-title">我的作品</div>
    <div class="grid no-scrollbar">
      <van-empty v-if="videos.length === 0" description="还没有发布作品" />
      <div v-for="v in videos" :key="v.id" class="grid-item" @click="preview(v)">
        <van-image v-if="v.coverUrl" width="100%" height="100%" fit="cover" :src="assetUrl(v.coverUrl)" />
        <div v-else class="cover-placeholder">
          <van-icon name="video" />
        </div>
        <van-icon name="delete-o" class="v-del" @click.stop="removeVideo(v)" />
        <div class="g-title">{{ v.title }}</div>
      </div>
    </div>

    <!-- 编辑资料 -->
    <van-popup v-model:show="editShow" teleport="#popup-root" position="bottom" round>
      <div class="edit-form">
        <van-cell-group inset>
          <van-field v-model="editForm.nickname" label="昵称" />
          <van-field v-model="editForm.bio" label="简介" type="textarea" rows="2" autosize />
          <van-field name="性别" label="性别">
            <template #input>
              <van-radio-group v-model="editForm.gender" direction="horizontal">
                <van-radio :value="1">男</van-radio>
                <van-radio :value="2">女</van-radio>
              </van-radio-group>
            </template>
          </van-field>
        </van-cell-group>
        <div class="edit-actions">
          <van-button block round type="primary" @click="saveEdit">保存</van-button>
        </div>
      </div>
    </van-popup>

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
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showSuccessToast } from 'vant'
import { useUserStore } from '../stores/user'
import { deleteVideo, getUserVideos } from '../api/modules/video'
import { getNotificationUnreadCount } from '../api/modules/notification'
import service from '../utils/request'
import { assetUrl } from '../utils/url'
import type { UserInfo, VideoItem } from '../types'

const router = useRouter()
const userStore = useUserStore()

const defaultAvatar =
  'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="68" height="68"><rect width="68" height="68" fill="%230e6b52"/><text x="34" y="42" font-size="26" fill="white" text-anchor="middle">%E4%BA%BA</text></svg>'

const user = ref<UserInfo | null>(userStore.userInfo)
const videos = ref<VideoItem[]>([])
const editShow = ref(false)
const previewShow = ref(false)
const previewItem = ref<VideoItem | null>(null)

const editForm = reactive({ nickname: '', bio: '', gender: 0 })
const unreadCount = ref(0)

onMounted(async () => {
  const info = await service.get('/user/info')
  user.value = info as UserInfo
  userStore.setUserInfo(info as UserInfo)
  const data = await getUserVideos(user.value.id, 1, 30)
  videos.value = data.list
  unreadCount.value = await getNotificationUnreadCount()
})

function openEdit() {
  editForm.nickname = user.value?.nickname || ''
  editForm.bio = user.value?.bio || ''
  editForm.gender = user.value?.gender || 0
  editShow.value = true
}

async function saveEdit() {
  await service.put('/user/update', { ...editForm })
  editShow.value = false
  showSuccessToast('已保存')
  const info = (await service.get('/user/info')) as UserInfo
  user.value = info
  userStore.setUserInfo(info)
}

function goPublish() {
  router.push('/publish')
}

function goNotifications() {
  router.push('/notifications')
}

function goFollow(tab: 'following' | 'followers') {
  const id = user.value?.id
  if (id) {
    router.push({ name: 'follow-list', params: { id }, query: { tab } })
  }
}

function logout() {
  userStore.clear()
  router.replace('/login')
}

function preview(v: VideoItem) {
  previewItem.value = v
  previewShow.value = true
}

async function removeVideo(v: VideoItem) {
  try {
    await showConfirmDialog({ title: '删除视频', message: '确定删除这条视频吗？' })
  } catch {
    // 用户取消
    return
  }
  await deleteVideo(v.id)
  videos.value = videos.value.filter((x) => x.id !== v.id)
  if (user.value && user.value.videoCount) {
    user.value.videoCount -= 1
    userStore.setUserInfo({ ...user.value })
  }
  showSuccessToast('已删除')
}
</script>

<style scoped>
.profile {
  height: 100%;
  overflow-y: auto;
  background: #0a0f1a;
}
.header {
  display: flex;
  gap: 14px;
  padding: calc(env(safe-area-inset-top) + 18px) 18px 16px;
  background: linear-gradient(180deg, #1e40af, #0a0f1a);
}
.notif-wrap {
  position: relative;
  flex: 1;
}
.notif-wrap .van-button {
  width: 100%;
}
.notif-badge {
  position: absolute;
  top: -6px;
  right: 6px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 8px;
  background: var(--huimai-primary);
  color: #fff;
  font-size: 10px;
  line-height: 16px;
  text-align: center;
}
.stat-link {
  cursor: pointer;
}
.meta {
  flex: 1;
}
.nickname {
  font-size: 19px;
  font-weight: 700;
}
.bio {
  color: var(--huimai-muted);
  font-size: 13px;
  margin: 6px 0;
}
.stats {
  display: flex;
  gap: 18px;
  font-size: 13px;
  color: var(--huimai-muted);
}
.stats b {
  color: #fff;
}
.publish-entry {
  padding: 4px 18px 12px;
}
.actions-row {
  display: flex;
  gap: 10px;
  padding: 4px 18px 12px;
}
.actions-row .van-button {
  flex: 1;
}
.section-title {
  padding: 8px 16px;
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
.v-del {
  position: absolute;
  top: 6px;
  right: 6px;
  z-index: 2;
  padding: 4px;
  font-size: 17px;
  color: #fff;
  background: rgba(0, 0, 0, 0.45);
  border-radius: 50%;
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
  font-size: 28px;
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
.edit-form {
  padding: 16px 0;
}
.edit-actions {
  padding: 18px 16px;
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
