// 后端接口基础地址：开发环境为相对路径 /api（走 Vite 代理），
// 生产构建为后端绝对地址（原生 App 直连）
const API_BASE = (import.meta.env.VITE_API_BASE as string) || ''

// 取出后端地址的 origin（协议+主机+端口），用于把相对媒体路径转成绝对地址。
// 开发环境 API_BASE 以 / 开头，origin 留空，媒体仍走相对路径。
function resolveOrigin(): string {
  if (!API_BASE || API_BASE.startsWith('/')) {
    return ''
  }
  try {
    return new URL(API_BASE).origin
  } catch {
    return ''
  }
}
const ORIGIN = resolveOrigin()

const ABSOLUTE_RE = /^(https?:|blob:|data:|capacitor:|ionic:)/

/**
 * 把后端返回的媒体相对路径（如 /api/media/cover/xxx.jpg）转成可在原生
 * WebView 中访问的绝对地址。已是绝对地址或空值时原样返回。
 */
export function assetUrl(path?: string | null): string {
  if (!path) {
    return ''
  }
  if (ABSOLUTE_RE.test(path)) {
    return path
  }
  return ORIGIN + path
}
