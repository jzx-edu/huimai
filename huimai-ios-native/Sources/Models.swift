import Foundation

// MARK: - 与后端 Project 一一对应的数据模型
// 字段名必须与 Spring Boot 返回的 JSON 完全一致（Jackson 默认 camelCase）

/// 统一返回结构 { code, message, data }
struct Envelope<T: Decodable>: Decodable {
    let code: Int
    let message: String?
    let data: T?
}

/// 分页结构 data.list / data.total / data.page / data.size
struct PageData<T: Decodable>: Decodable {
    let list: [T]
    let total: Int
    let page: Int?
    let size: Int?
}

struct VideoItem: Decodable, Identifiable, Equatable {
    let id: Int64
    let title: String
    let description: String?
    let videoUrl: String
    let coverUrl: String?
    let duration: Int64?
    let width: Int?
    let height: Int?
    let isKnowledge: Int?
    let status: Int?

    let playCount: Int64?
    let likeCount: Int64?
    let commentCount: Int64?
    let favoriteCount: Int64?
    let shareCount: Int64?

    let authorId: Int64
    let authorName: String
    let authorAvatar: String?

    let categoryId: Int64?
    let categoryName: String?

    let liked: Bool?
    let favorited: Bool?

    let createdAt: String?
}

struct LoginResult: Decodable {
    let token: String
    let userId: Int64
    let username: String
    let nickname: String
    let avatar: String?
}

struct LikeResult: Decodable {
    let liked: Bool
}

struct FavoriteResult: Decodable {
    let favorited: Bool
}
