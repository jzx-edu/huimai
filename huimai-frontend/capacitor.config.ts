import type { CapacitorConfig } from '@capacitor/cli'

const config: CapacitorConfig = {
  // 应用唯一标识（Bundle ID），上架 App Store 后不可更改
  appId: 'com.huimai.app',
  appName: '回脉',
  // Vite 构建产物目录
  webDir: 'dist',
  ios: {
    // 让 WebView 内容自动考虑安全区，配合 CSS env(safe-area-inset-*)
    contentInset: 'always'
  }
}

export default config
