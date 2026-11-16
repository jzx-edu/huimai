/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** 后端接口基础地址：开发为 /api，生产为后端绝对地址 */
  readonly VITE_API_BASE: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}
