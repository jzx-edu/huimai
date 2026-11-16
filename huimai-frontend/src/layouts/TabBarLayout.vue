<template>
  <div class="tab-layout">
    <div class="tab-content">
      <router-view />
    </div>
    <van-tabbar
      route
      active-color="#ffffff"
      inactive-color="#8a94a6"
      safe-area-inset-bottom
      class="custom-tabbar"
    >
      <van-tabbar-item to="/feed" icon="wap-home-o"></van-tabbar-item>
      <van-tabbar-item to="/discover" icon="apps-o"></van-tabbar-item>
      <van-tabbar-item to="/shop" icon="shop-o"></van-tabbar-item>
      <van-tabbar-item to="/profile" icon="user-o"></van-tabbar-item>
    </van-tabbar>

    <!-- 消息（视频互动）入口：悬浮夹在 4 个菜单正中间，不参与均分；
         非信息流页平滑淡化并禁用，4 个主导航项位置始终不变 -->
    <button
      type="button"
      class="message-fab"
      :class="{ dimmed: !messageActive }"
      aria-label="视频互动"
      @click="openAction"
    >
      <van-icon name="comment-o" />
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useActionStore } from '../stores/action'

const route = useRoute()
const actionStore = useActionStore()

// 消息是“针对当前视频”的功能，仅在信息流页面存在
const isFeed = computed(() => route.name === 'feed')

// 消息按钮点亮条件：信息流页且当前视频正在播放（而非仅仅停留在首页）
const messageActive = computed(() => isFeed.value && actionStore.videoPlaying)

// 点击消息图标：不跳页面，唤起当前视频的互动操作面板
function openAction() {
  actionStore.requestOpen()
}
</script>

<style scoped>
.tab-layout {
  position: relative;
  height: 100%;
  overflow: hidden;
}

/* 内容区占满整个容器，视频可铺满到底部，不再为底栏预留空间 */
.tab-content {
  position: absolute;
  inset: 0;
  height: 100%;
  overflow: hidden;
}

/* 底栏悬浮于视频之上：透明底 + 自下而上的黑色渐变，
   既保证图标清晰，又让视频内容透出、不被实色遮挡 */
.custom-tabbar {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    to top,
    rgba(0, 0, 0, 0.62) 0%,
    rgba(0, 0, 0, 0.3) 55%,
    rgba(0, 0, 0, 0) 100%
  );
  z-index: 20;
  /* 去除点击图标时的白色方块背景 */
  --van-tabbar-item-active-background: transparent;
}

/* 强制覆盖按下瞬间的反馈背景，确保不出现白色方块 */
.custom-tabbar :deep(.van-tabbar-item:active) {
  background-color: transparent;
}

/* 去除底栏 hairline 细线（van-hairline--top-bottom 的 ::after 边框） */
.custom-tabbar::after {
  border: none !important;
}

/* 纯图标导航：图标在栏内垂直居中 */
.custom-tabbar :deep(.van-tabbar-item__icon) {
  margin-bottom: 0;
  font-size: 23px;
}

/* 消息（视频互动）悬浮按钮：绝对定位在底栏正中间，不参与 4 菜单均分，
   夹在“发现”和“发布”之间，4 个主导航项的位置不受它影响 */
.message-fab {
  position: absolute;
  left: 50%;
  bottom: calc(4px + constant(safe-area-inset-bottom));
  bottom: calc(4px + env(safe-area-inset-bottom));
  transform: translateX(-50%);
  width: 42px;
  height: 42px;
  padding: 0;
  border: none;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 21px;
  box-shadow: 0 3px 12px rgba(37, 99, 235, 0.55);
  z-index: 21;
  cursor: pointer;
  transition: opacity 0.3s ease, visibility 0.3s ease;
}

/* 视频未播放（暂停或非信息流页）时：直接隐藏（平滑淡出），
   并禁用点击，避免布局/视觉跳动 */
.message-fab.dimmed {
  opacity: 0;
  visibility: hidden;
  pointer-events: none;
}
</style>
