import type { CapacitorConfig } from '@capacitor/cli'

const config: CapacitorConfig = {
  // 应用唯一标识（Bundle ID），上架 App Store 后不可更改
  appId: 'com.huimai.app',
  appName: '回脉',
  // Vite 构建产物目录
  webDir: 'dist',
  ios: {
    // 视频类 App 采用 edge-to-edge 全屏：WebView 内容延伸到状态栏与 Home Indicator 下方，
    // 避免 contentInset=always 在顶部/底部留出安全区空白（露出 WebView 背景形成白边）。
    // 悬浮底栏、进度条等已各自用 env(safe-area-inset-*) 做安全区适配。
    contentInset: 'never'
  }
}

export default config
