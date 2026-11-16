<template>
  <div class="discover">
    <div class="header safe-bottom">
      <van-search
        v-model="keyword"
        placeholder="搜索视频、主题、作者"
        shape="round"
        @search="onSearch"
      />
      <!-- 历史搜索：独立板块（卡片式），始终显示 -->
      <div class="history-panel">
        <div class="panel-head">
          <div class="panel-label">
            <van-icon name="clock-o" />
            <span>历史搜索</span>
          </div>
          <div v-if="history.length" class="panel-clear" @click="clearHistory">
            <van-icon name="delete-o" />
            <span>清空</span>
          </div>
        </div>
        <div v-if="history.length" class="panel-list">
          <div v-for="(h, i) in history" :key="i" class="history-row">
            <van-icon name="search" class="row-icon" />
            <span class="row-text" @click="useHistory(h)">{{ h }}</span>
            <van-icon name="cross" class="row-del" @click="removeHistory(i)" />
          </div>
        </div>
        <div v-else class="panel-empty">暂无历史搜索</div>
      </div>
      <div class="chips no-scrollbar">
        <span
          v-for="c in categories"
          :key="c.id"
          class="chip"
          :class="{ active: activeCategory === c.id }"
          @click="selectCategory(c.id)"
        >
          {{ c.name }}
        </span>
      </div>
    </div>

    <div v-if="userResults.length" class="user-result">
      <div class="ur-label">用户</div>
      <div
        v-for="u in userResults"
        :key="u.id"
        class="ur-item"
        @click="goUserHome(u.id)"
      >
        <van-image round width="44" height="44" :src="assetUrl(u.avatar) || defaultAvatar" />
        <div class="ur-main">
          <div class="ur-name">{{ u.nickname }}</div>
          <div class="ur-bio">{{ u.bio || '这个人还没有简介' }}</div>
        </div>
        <van-icon name="arrow" class="ur-arrow" />
      </div>
    </div>

    <div class="grid no-scrollbar">
      <van-empty
        v-if="!loading && videos.length === 0"
        :description="keyword.trim() ? '没有相关视频' : `${activeCategoryName}频道暂无内容，敬请期待`"
      >
        <van-button
          v-if="!keyword.trim()"
          round
          size="small"
          type="primary"
          @click="goFeed"
        >去首页看看</van-button>
      </van-empty>
      <div v-for="v in videos" :key="v.id" class="grid-item" @click="preview(v)">
        <van-image
          width="100%"
          height="100%"
          fit="cover"
          :src="assetUrl(v.coverUrl || v.videoUrl)"
        />
        <div class="caption">
          <van-icon name="play" /> {{ formatCount(v.playCount) }}
        </div>
        <div class="g-title">{{ v.title }}</div>
      </div>
    </div>

    <van-popup
      v-model:show="previewShow"
      teleport="#popup-root"
      :style="{ width: '100%', height: '100%' }"
      class="preview-pop"
    >
      <div class="preview-box">
        <video
          v-if="previewVideo"
          :src="assetUrl(previewVideo.videoUrl)"
          :poster="assetUrl(previewVideo.coverUrl)"
          controls
          autoplay
          playsinline
          class="preview-video"
        />
        <div class="preview-info">
          <div class="p-title">{{ previewVideo?.title }}</div>
          <div class="p-author">@{{ previewVideo?.authorName }}</div>
          <div v-if="previewVideo?.description" class="p-desc">
            {{ previewVideo.description }}
          </div>
        </div>
        <van-icon name="cross" class="close" @click="previewShow = false" />
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getCategories } from '../api/modules/category'
import { getByCategory, searchVideos } from '../api/modules/video'
import { searchUsers } from '../api/modules/user'
import { assetUrl } from '../utils/url'
import type { CategoryItem, UserInfo, VideoItem } from '../types'

const router = useRouter()

const keyword = ref('')
const categories = ref<CategoryItem[]>([])
const activeCategory = ref(0)
const videos = ref<VideoItem[]>([])
const userResults = ref<UserInfo[]>([])
const loading = ref(false)
const previewShow = ref(false)
const previewVideo = ref<VideoItem | null>(null)

const defaultAvatar =
  'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="44" height="44"><rect width="44" height="44" rx="22" fill="%231e3a8a"/><text x="22" y="29" font-size="16" fill="white" text-anchor="middle">%E4%BA%BA</text></svg>'

// 历史搜索记录：持久化到 localStorage，刷新后保留
const HISTORY_KEY = 'huimai_search_history'
const HISTORY_MAX = 10
const history = ref<string[]>([])

function loadHistory() {
  try {
    const raw = localStorage.getItem(HISTORY_KEY)
    history.value = raw ? (JSON.parse(raw) as string[]) : []
  } catch {
    history.value = []
  }
}

function persistHistory() {
  localStorage.setItem(HISTORY_KEY, JSON.stringify(history.value))
}

// 新增一条历史：去重并把最新放在最前，超过上限截断
function addHistory(term: string) {
  const t = term.trim()
  if (!t) {
    return
  }
  history.value = [t, ...history.value.filter((x) => x !== t)].slice(0, HISTORY_MAX)
  persistHistory()
}

function clearHistory() {
  history.value = []
  localStorage.removeItem(HISTORY_KEY)
}

// 删除单条历史
function removeHistory(index: number) {
  history.value.splice(index, 1)
  persistHistory()
}

// 点击历史词：回填关键词并直接搜索
function useHistory(term: string) {
  keyword.value = term
  onSearch()
}

onMounted(async () => {
  loadHistory()
  categories.value = await getCategories()
  // 不再有“全部”，默认选中第一个频道
  if (categories.value.length > 0) {
    activeCategory.value = categories.value[0].id
  }
  await load()
})

async function load() {
  loading.value = true
  try {
    const data = await getByCategory(activeCategory.value, 1, 30)
    videos.value = data.list
  } finally {
    loading.value = false
  }
}

function selectCategory(id: number) {
  activeCategory.value = id
  keyword.value = ''
  userResults.value = []
  load()
}

async function onSearch() {
  const term = keyword.value.trim()
  if (!term) {
    userResults.value = []
    return load()
  }
  addHistory(term)
  loading.value = true
  try {
    const [data, users] = await Promise.all([
      searchVideos(term, 1, 30),
      searchUsers(term)
    ])
    videos.value = data.list
    userResults.value = users
  } finally {
    loading.value = false
  }
}

function goUserHome(id: number) {
  router.push(`/user/${id}`)
}

function goFeed() {
  router.push('/feed')
}

const activeCategoryName = computed(
  () => categories.value.find((x) => x.id === activeCategory.value)?.name || ''
)

function preview(v: VideoItem) {
  previewVideo.value = v
  previewShow.value = true
}

function formatCount(n: number) {
  return n >= 10000 ? (n / 10000).toFixed(1) + 'w' : String(n)
}
</script>

<style scoped>
.discover {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #0a0f1a;
}
.header {
  background: #0f1524;
}
/* 历史搜索独立板块：卡片式，区别于 header 背景 */
.history-panel {
  margin: 0 12px 10px;
  padding: 12px;
  border-radius: 12px;
  background: #141b2c;
  border: 1px solid #1f2940;
}
.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}
.panel-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #eef2f9;
}
.panel-clear {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #9aa7bd;
}
.panel-list {
  display: flex;
  flex-direction: column;
}
.history-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 0;
}
.row-icon {
  font-size: 14px;
  color: #6b7891;
}
.row-text {
  flex: 1;
  font-size: 13px;
  color: #c3ccdb;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.row-del {
  font-size: 14px;
  color: #6b7891;
}
.panel-empty {
  padding: 10px 0 4px;
  font-size: 12px;
  color: #6b7891;
}
.chips {
  display: flex;
  gap: 8px;
  padding: 0 12px 10px;
  overflow-x: auto;
}
.chip {
  flex-shrink: 0;
  padding: 5px 14px;
  border-radius: 16px;
  font-size: 13px;
  background: #1c2434;
  color: #c3ccdb;
}
.chip.active {
  background: var(--huimai-primary);
  color: #fff;
}
.user-result {
  max-height: 230px;
  overflow-y: auto;
  margin: 8px 12px 4px;
  padding: 10px 12px;
  border-radius: 12px;
  background: #141b2c;
  border: 1px solid #1f2940;
}
.ur-label {
  font-size: 13px;
  font-weight: 600;
  color: #9aa7bd;
  margin-bottom: 4px;
}
.ur-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
}
.ur-item:active {
  opacity: 0.8;
}
.ur-main {
  flex: 1;
  min-width: 0;
}
.ur-name {
  font-size: 14px;
  font-weight: 600;
}
.ur-bio {
  margin-top: 3px;
  font-size: 12px;
  color: #9aa7bd;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.ur-arrow {
  color: #9aa7bd;
  font-size: 15px;
}
.grid {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  align-content: start;
}
.grid-item {
  position: relative;
  aspect-ratio: 9 / 16;
  border-radius: 8px;
  overflow: hidden;
  background: #000;
}
.caption {
  position: absolute;
  top: 6px;
  left: 6px;
  font-size: 11px;
  color: #fff;
  text-shadow: 0 1px 2px #000;
}
.g-title {
  position: absolute;
  left: 6px;
  right: 6px;
  bottom: 6px;
  font-size: 12px;
  color: #fff;
  text-shadow: 0 1px 3px #000;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.preview-box {
  position: relative;
  height: 100%;
  background: #000;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.preview-video {
  width: 100%;
  max-height: 75%;
  object-fit: contain;
}
.preview-info {
  padding: 14px;
}
.p-title {
  font-weight: 700;
  margin-bottom: 6px;
}
.p-author {
  color: var(--huimai-gold);
  font-size: 13px;
  margin-bottom: 6px;
}
.p-desc {
  color: #c3ccdb;
  font-size: 13px;
}
.close {
  position: absolute;
  top: calc(env(safe-area-inset-top) + 12px);
  right: 16px;
  font-size: 26px;
  color: #fff;
}
</style>
