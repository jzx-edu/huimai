<template>
  <div class="follow-page">
    <div class="nav">
      <van-icon name="arrow-left" @click="back" />
      <div class="tabs">
        <span class="tab" :class="{ active: tab === 'following' }" @click="switchTab('following')">
          关注
        </span>
        <span class="tab" :class="{ active: tab === 'followers' }" @click="switchTab('followers')">
          粉丝
        </span>
      </div>
    </div>

    <van-empty v-if="!loading && list.length === 0" :description="tab === 'following' ? '还没有关注' : '还没有粉丝'" />

    <div v-else class="user-list no-scrollbar">
      <div v-for="u in list" :key="u.id" class="user-item" @click="goHome(u.id)">
        <van-image round width="48" height="48" :src="assetUrl(u.avatar) || defaultAvatar" />
        <div class="u-main">
          <div class="u-name">{{ u.nickname }}</div>
          <div class="u-bio">{{ u.bio || '这个人还没有简介' }}</div>
        </div>
        <van-icon name="arrow" class="u-arrow" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getFollowersList, getFollowingList } from '../api/modules/user'
import { assetUrl } from '../utils/url'
import type { UserInfo } from '../types'

const route = useRoute()
const router = useRouter()

const defaultAvatar =
  'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="48" height="48"><rect width="48" height="48" rx="24" fill="%231e3a8a"/><text x="24" y="31" font-size="18" fill="white" text-anchor="middle">%E4%BA%BA</text></svg>'

const targetId = Number(route.params.id)
const tab = ref<'following' | 'followers'>(
  route.query.tab === 'followers' ? 'followers' : 'following'
)
const list = ref<UserInfo[]>([])
const loading = ref(true)

onMounted(() => load())

async function load() {
  loading.value = true
  try {
    list.value = tab.value === 'following'
      ? await getFollowingList(targetId)
      : await getFollowersList(targetId)
  } finally {
    loading.value = false
  }
}

function switchTab(t: 'following' | 'followers') {
  if (tab.value === t) {
    return
  }
  tab.value = t
  load()
}

function goHome(id: number) {
  router.push(`/user/${id}`)
}

function back() {
  router.back()
}
</script>

<style scoped>
.follow-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--huimai-bg);
}
.nav {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: calc(env(safe-area-inset-top) + 12px) 16px 10px;
  font-size: 20px;
}
.tabs {
  display: flex;
  gap: 24px;
}
.tab {
  font-size: 15px;
  color: var(--huimai-muted);
  padding-bottom: 4px;
}
.tab.active {
  color: var(--huimai-text);
  font-weight: 700;
  border-bottom: 2px solid var(--huimai-primary);
}
.user-list {
  flex: 1;
  overflow-y: auto;
  padding-bottom: calc(var(--tabbar-height) + env(safe-area-inset-bottom) + 12px);
}
.user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
}
.user-item:active {
  background: rgba(255, 255, 255, 0.04);
}
.u-main {
  flex: 1;
  min-width: 0;
}
.u-name {
  font-size: 15px;
  font-weight: 600;
}
.u-bio {
  margin-top: 4px;
  font-size: 13px;
  color: var(--huimai-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.u-arrow {
  color: var(--huimai-muted);
  font-size: 16px;
}
</style>
