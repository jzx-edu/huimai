import Foundation
import Combine

/// 登录态：token 与用户信息，持久化到 UserDefaults
final class Session: ObservableObject {
    static let shared = Session()

    @Published private(set) var token: String?
    @Published private(set) var nickname: String?
    @Published private(set) var avatar: String?

    private let tokenKey = "huimai.token"
    private let nicknameKey = "huimai.nickname"
    private let avatarKey = "huimai.avatar"

    init() {
        let defaults = UserDefaults.standard
        token = defaults.string(forKey: tokenKey)
        nickname = defaults.string(forKey: nicknameKey)
        avatar = defaults.string(forKey: avatarKey)
    }

    var isLoggedIn: Bool {
        guard let token else { return false }
        return !token.isEmpty
    }

    func save(_ result: LoginResult) {
        let defaults = UserDefaults.standard
        defaults.set(result.token, forKey: tokenKey)
        defaults.set(result.nickname, forKey: nicknameKey)
        defaults.set(result.avatar, forKey: avatarKey)
        token = result.token
        nickname = result.nickname
        avatar = result.avatar
    }

    func clear() {
        let defaults = UserDefaults.standard
        defaults.removeObject(forKey: tokenKey)
        defaults.removeObject(forKey: nicknameKey)
        defaults.removeObject(forKey: avatarKey)
        token = nil
        nickname = nil
        avatar = nil
    }
}
