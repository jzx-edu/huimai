import { onBeforeUnmount, onMounted } from 'vue'

/**
 * 定时轮询。
 * - 组件挂载后启动，卸载前自动清除
 * - 页面切到后台（document.hidden）时暂停，回到前台时立即执行一次并恢复
 */
export function usePolling(task: () => void | Promise<void>, intervalMs: number) {
  let timer: ReturnType<typeof setInterval> | null = null

  function stop() {
    if (timer !== null) {
      clearInterval(timer)
      timer = null
    }
  }

  function start() {
    if (timer !== null) {
      return
    }
    timer = setInterval(() => {
      void task()
    }, intervalMs)
  }

  function onVisibility() {
    if (document.hidden) {
      stop()
    } else {
      void task()
      start()
    }
  }

  onMounted(() => {
    start()
    document.addEventListener('visibilitychange', onVisibility)
  })

  onBeforeUnmount(() => {
    stop()
    document.removeEventListener('visibilitychange', onVisibility)
  })

  return { stop, start }
}
