<template>
  <div class="feed-view">
    <div
      class="feed-container no-scrollbar"
      ref="containerRef"
      @scroll="onScroll"
    >
      <div
        v-for="(item, index) in videoList"
        :key="item.id"
        class="feed-item"
        :data-index="index"
        :ref="setItemRef"
      >
        <VideoSwipeItem :video="item" :active="activeIndex === index" />
      </div>

      <div class="loading-tip">
        <van-loading v-if="loading" size="20">加载中...</van-loading>
        <span v-else-if="!hasMore && videoList.length > 0">已经到底了</span>
        <van-empty v-if="!loading && videoList.length === 0" description="暂无视频" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import VideoSwipeItem from '../components/VideoSwipeItem.vue'
import { getFeed, reportPlay } from '../api/modules/video'
import type { VideoItem } from '../types'

const containerRef = ref<HTMLElement>()
const itemEls = ref<HTMLElement[]>([])
const videoList = ref<VideoItem[]>([])
const activeIndex = ref(0)
const page = ref(0)
const size = 8
const hasMore = ref(true)
const loading = ref(false)
const reported = new Set<number>()

let observer: IntersectionObserver | null = null

function setItemRef(el: unknown) {
  if (el instanceof HTMLElement) {
    itemEls.value.push(el)
  }
}

onMounted(async () => {
  await loadMore()
  initObserver()
})

onBeforeUnmount(() => observer?.disconnect())

async function loadMore() {
  if (loading.value || !hasMore.value) {
    return
  }
  loading.value = true
  try {
    page.value += 1
    const data = await getFeed(page.value, size)
    videoList.value.push(...data.list)
    hasMore.value = videoList.value.length < data.total
    await nextTick()
    rebuildObserver()
  } finally {
    loading.value = false
  }
}

function initObserver() {
  if (!containerRef.value) {
    return
  }
  observer = new IntersectionObserver(
    (entries) => {
      for (const entry of entries) {
        if (entry.isIntersecting && entry.intersectionRatio >= 0.6) {
          const idx = Number((entry.target as HTMLElement).dataset.index)
          if (activeIndex.value !== idx) {
            activeIndex.value = idx
          }
          const video = videoList.value[idx]
          if (video && !reported.has(video.id)) {
            reported.add(video.id)
            reportPlay(video.id)
          }
        }
      }
    },
    { root: containerRef.value, threshold: [0.6] }
  )
  itemEls.value.forEach((el) => observer?.observe(el))
}

function rebuildObserver() {
  observer?.disconnect()
  itemEls.value = []
  nextTick(() => {
    // 重新收集 DOM 后观察
    const nodes = containerRef.value?.querySelectorAll('.feed-item')
    itemEls.value = Array.from(nodes || []) as HTMLElement[]
    itemEls.value.forEach((el) => observer?.observe(el))
  })
}

function onScroll() {
  const el = containerRef.value
  if (!el) {
    return
  }
  if (el.scrollTop + el.clientHeight >= el.scrollHeight - 120) {
    loadMore()
  }
}
</script>

<style scoped>
.feed-view {
  position: relative;
  height: 100%;
  background: #000;
}
.feed-container {
  height: 100%;
  overflow-y: scroll;
  scroll-snap-type: y mandatory;
}
.feed-item {
  height: 100%;
  scroll-snap-align: start;
  scroll-snap-stop: always;
}
.loading-tip {
  text-align: center;
  padding: 16px;
  color: var(--huimai-muted);
  font-size: 13px;
}
</style>
