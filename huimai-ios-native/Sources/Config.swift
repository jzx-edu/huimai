import Foundation

/// 全局配置：后端地址与媒体地址拼装
enum AppConfig {
    private static let apiBaseKey = "huimai.apiBase"

    /// 后端接口前缀（可在「我的」页里改，方便电脑 IP 变化后无需重新打包）
    static var apiBase: String {
        get {
            UserDefaults.standard.string(forKey: apiBaseKey) ?? "http://192.168.1.7:9090/api"
        }
        set {
            UserDefaults.standard.set(newValue, forKey: apiBaseKey)
        }
    }

    /// 后端 origin（协议 + 主机 + 端口），用于把 /api/media/xxx 这种相对媒体路径转成绝对地址
    static var origin: String {
        let base = apiBase
        if let range = base.range(of: "/api", options: .backwards) {
            return String(base[..<range.lowerBound])
        }
        return base
    }

    /// 媒体地址（视频、封面、头像）
    static func mediaURL(_ path: String?) -> URL? {
        guard let path, !path.isEmpty else { return nil }
        if path.hasPrefix("http://") || path.hasPrefix("https://") {
            return URL(string: path)
        }
        if path.hasPrefix("/") {
            return URL(string: origin + path)
        }
        return URL(string: origin + "/" + path)
    }
}
