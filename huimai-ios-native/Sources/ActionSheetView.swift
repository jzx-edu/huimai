import SwiftUI

/// 视频互动面板（第一阶段：作者信息 + 点赞 + 收藏）
/// 评论、私信、转发在第二阶段接入
struct ActionSheetView: View {
    @ObservedObject var feed: FeedViewModel
    @ObservedObject private var session = Session.shared
    @Environment(\.dismiss) private var dismiss

    @State private var liked = false
    @State private var favorited = false
    @State private var likeCount: Int64 = 0
    @State private var favoriteCount: Int64 = 0
    @State private var commentCount: Int64 = 0
    @State private var hint: String?
    @State private var isBusy = false

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Capsule()
                .fill(Color.white.opacity(0.2))
                .frame(width: 38, height: 4)
                .frame(maxWidth: .infinity)
                .padding(.top, 8)

            if let video = feed.activeVideo {
                HStack(spacing: 12) {
                    avatar(video)
                    VStack(alignment: .leading, spacing: 3) {
                        Text(video.authorName)
                            .font(.system(size: 15, weight: .semibold))
                            .foregroundStyle(.white)
                        Text(video.title)
                            .font(.system(size: 12))
                            .foregroundStyle(Color.huimaiMuted)
                            .lineLimit(2)
                    }
                    Spacer()
                }

                HStack(spacing: 10) {
                    actionButton(
                        title: "点赞",
                        value: likeCount,
                        systemImage: liked ? "hand.thumbsup.fill" : "hand.thumbsup",
                        active: liked
                    ) {
                        Task { await toggleLike(video) }
                    }

                    actionButton(
                        title: "收藏",
                        value: favoriteCount,
                        systemImage: favorited ? "star.fill" : "star",
                        active: favorited
                    ) {
                        Task { await toggleFavorite(video) }
                    }

                    infoBadge(title: "评论", value: commentCount)
                }

                Text(hint ?? (session.isLoggedIn ? "评论、私信、转发将在第二阶段接入原生实现" : "登录后即可点赞与收藏"))
                    .font(.system(size: 11))
                    .foregroundStyle(hint == nil ? Color.huimaiMuted : Color(red: 0.91, green: 0.33, blue: 0.29))

                Spacer(minLength: 0)
            } else {
                Text("当前没有可操作的作品")
                    .font(.system(size: 13))
                    .foregroundStyle(Color.huimaiMuted)
                Spacer(minLength: 0)
            }
        }
        .padding(.horizontal, 20)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.huimaiBackground.ignoresSafeArea())
        .presentationDetents([.medium])
        .presentationDragIndicator(.hidden)
        .onAppear(perform: loadState)
    }

    private func avatar(_ video: VideoItem) -> some View {
        Group {
            if let url = AppConfig.mediaURL(video.authorAvatar) {
                AsyncImage(url: url) { image in
                    image.resizable().scaledToFill()
                } placeholder: {
                    Circle().fill(Color.huimaiPrimary.opacity(0.2))
                }
            } else {
                Circle().fill(Color.huimaiPrimary.opacity(0.2))
            }
        }
        .frame(width: 44, height: 44)
        .clipShape(Circle())
    }

    private func actionButton(title: String, value: Int64, systemImage: String, active: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            VStack(spacing: 4) {
                Image(systemName: systemImage)
                    .font(.system(size: 20, weight: .medium))
                Text("\(value)")
                    .font(.system(size: 12))
            }
            .foregroundStyle(active ? Color.huimaiPrimary : Color.white.opacity(0.85))
            .frame(width: 78, height: 60)
            .background(Color.huimaiCard)
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
        .buttonStyle(.plain)
        .disabled(isBusy)
    }

    private func infoBadge(title: String, value: Int64) -> some View {
        VStack(spacing: 4) {
            Image(systemName: "bubble.right")
                .font(.system(size: 20, weight: .medium))
            Text("\(value)")
                .font(.system(size: 12))
        }
        .foregroundStyle(Color.white.opacity(0.55))
        .frame(width: 78, height: 60)
        .background(Color.huimaiCard)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }

    private func loadState() {
        guard let video = feed.activeVideo else { return }
        liked = video.liked ?? false
        favorited = video.favorited ?? false
        likeCount = video.likeCount ?? 0
        favoriteCount = video.favoriteCount ?? 0
        commentCount = video.commentCount ?? 0
    }

    private func toggleLike(_ video: VideoItem) async {
        guard requireLogin() else { return }
        isBusy = true
        defer { isBusy = false }
        do {
            let result: LikeResult = try await APIClient.shared.post("/interaction/like/\(video.id)")
            liked = result.liked
            likeCount += result.liked ? 1 : -1
            hint = nil
        } catch {
            hint = (error as? LocalizedError)?.errorDescription ?? "操作失败"
        }
    }

    private func toggleFavorite(_ video: VideoItem) async {
        guard requireLogin() else { return }
        isBusy = true
        defer { isBusy = false }
        do {
            let result: FavoriteResult = try await APIClient.shared.post("/interaction/favorite/\(video.id)")
            favorited = result.favorited
            favoriteCount += result.favorited ? 1 : -1
            hint = nil
        } catch {
            hint = (error as? LocalizedError)?.errorDescription ?? "操作失败"
        }
    }

    private func requireLogin() -> Bool {
        if session.isLoggedIn { return true }
        hint = "请先在「我的」页登录"
        return false
    }
}
