import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { Capacitor } from '@capacitor/core'
import { StatusBar, Style } from '@capacitor/status-bar'
import Vant from 'vant'
import 'vant/lib/index.css'
import App from './App.vue'
import router from './router'
import './styles/global.css'

// 仅在原生 iOS/Android 生效：深色主题下状态栏文字用浅色；
// 不隐藏状态栏，页面内容通过 CSS env(safe-area-inset-*) 避让
if (Capacitor.isNativePlatform()) {
  StatusBar.setStyle({ style: Style.Dark })
}

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(Vant)
app.mount('#app')
