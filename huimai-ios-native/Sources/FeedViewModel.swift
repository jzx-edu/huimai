import Combine
import Foundation

/// 信息流数据与播放状态的管理者
@MainActor
final class FeedViewModel: ObservableObject {
    @Published var videos: [VideoItem] = []
    @Published var activeID: Int64?
    @Published var isLoading = false
    @Published var errorText: String?

    let player = PlayerController()

    private var page = 0
    private let pageSize = 8
    private var hasMore = true
    private var reported: Set<Int64> = []
    private var didLongPress = false

    var activeVideo: VideoItem? {
        guard let activeID else { return nil }
        return videos.first { $0.id == activeID }
    }

    func loadFirstIfNeeded() async {
        guard videos.isEmpty, !isLoading else { return }
        await loadMore()
    }

    func loadMore() async {
        guard !isLoading, hasMore else { return }
        isLoading = true
        defer { isLoading = false }
        do {
            let next = page + 1
            let data: PageData<VideoItem> = try await APIClient.shared.get(
                "/videos/feed",
                query: ["page": "\(next)", "size": "\(pageSize)"]
            )
            page = next
            videos.append(contentsOf: data.list)
            hasMore = videos.count < data.total
            if activeID == nil {
                activeID = videos.first?.id
            }
            errorText = nil
            syncPlayer()
        } catch {
            errorText = (error as? LocalizedError)?.errorDescription ?? "加载失败"
        }
    }

    /// 当前页滚动到末尾时预加载下一页
    func loadMoreIfNeeded(currentID: Int64) {
        guard let index = videos.firstIndex(where: { $0.id == currentID }) else { return }
        if index >= videos.count - 3 {
            Task { await loadMore() }
        }
    }

    /// 根据当前激活视频切换播放器内容，并上报一次播放
    func syncPlayer() {
        guard let video = activeVideo, let url = AppConfig.mediaURL(video.videoUrl) else {
            player.pause()
            return
        }
        player.load(id: video.id, url: url, autoplay: true)
        if !reported.contains(video.id) {
            reported.insert(video.id)
            Task { await APIClient.shared.postVoid("/videos/\(video.id)/play") }
        }
    }

    // MARK: - 手势

    func handleTap() {
        // 长按结束松手时系统可能补发一次点击，这里吞掉，避免误触发暂停/播放
        if didLongPress {
            didLongPress = false
            return
        }
        player.togglePlay()
    }

    func beginLongPress() {
        didLongPress = true
        player.startFast()
    }

    func endLongPress() {
        player.stopFast()
        Task { [weak self] in
            try? await Task.sleep(nanoseconds: 300_000_000)
            self?.didLongPress = false
        }
    }

    func pause() {
        player.stopFast()
        player.pause()
    }
}
