import Foundation

struct APIError: LocalizedError {
    let message: String
    var errorDescription: String? { message }
}

struct EmptyPayload: Decodable {}

/// 极简 HTTP 客户端：拆解后端统一返回结构，自动附带 JWT
final class APIClient {
    static let shared = APIClient()
    private init() {}

    func get<T: Decodable>(_ path: String, query: [String: String] = [:]) async throws -> T {
        try await send(path: path, method: "GET", query: query, body: nil)
    }

    func post<T: Decodable>(_ path: String, body: [String: Any]? = nil) async throws -> T {
        try await send(path: path, method: "POST", query: [:], body: body)
    }

    /// 不关心返回数据的接口（如上报播放）
    func postVoid(_ path: String, body: [String: Any]? = nil) async {
        _ = try? await send(path: path, method: "POST", query: [:], body: body) as EmptyPayload
    }

    private func send<T: Decodable>(path: String, method: String, query: [String: String], body: [String: Any]?) async throws -> T {
        guard var comps = URLComponents(string: AppConfig.apiBase + path) else {
            throw APIError(message: "接口地址无效")
        }
        if !query.isEmpty {
            comps.queryItems = query.map { URLQueryItem(name: $0.key, value: $0.value) }
        }
        guard let url = comps.url else {
            throw APIError(message: "接口地址无效")
        }

        var request = URLRequest(url: url)
        request.httpMethod = method
        request.timeoutInterval = 30
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        if let token = Session.shared.token, !token.isEmpty {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        if let body {
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
            request.httpBody = try JSONSerialization.data(withJSONObject: body, options: [])
        }

        let (data, _) = try await URLSession.shared.data(for: request)

        guard let envelope = try? JSONDecoder().decode(Envelope<T>.self, from: data) else {
            throw APIError(message: "返回数据解析失败")
        }
        if envelope.code == 401 {
            Session.shared.clear()
        }
        guard envelope.code == 200 else {
            throw APIError(message: envelope.message ?? "请求失败（\(envelope.code)）")
        }
        guard let payload = envelope.data else {
            throw APIError(message: envelope.message ?? "无返回数据")
        }
        return payload
    }
}
