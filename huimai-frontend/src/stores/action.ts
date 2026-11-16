import { defineStore } from 'pinia'
import { ref } from 'vue'

// 视频互动面板的全局唤起信号：
// 底栏“消息”图标在布局层，当前视频在信息流层，用该 store 跨层传递“打开面板”
export const useActionStore = defineStore('video-action', () => {
  // 每次点击消息图标自增，作为“打开面板”的触发信号（用计数器保证重复点击也能触发）
  const openSignal = ref(0)

  // 当前信息流视频是否正在播放：供底栏“消息”按钮决定是否点亮
  const videoPlaying = ref(false)

  function requestOpen() {
    openSignal.value += 1
  }

  function setPlaying(playing: boolean) {
    videoPlaying.value = playing
  }

  return { openSignal, videoPlaying, requestOpen, setPlaying }
})
