import AVFoundation
import Combine
import Foundation

/// 单个 AVPlayer 的播放控制器：进度、循环、长按倍速、拖动 seek
/// 全局只用一个 player，切换视频时替换 item，性能最好
final class PlayerController: ObservableObject {
    let player = AVPlayer()

    @Published var isPlaying = false
    /// 当前播放进度 0~1
    @Published var progress: Double = 0
    @Published var duration: Double = 0
    /// 是否正在拖动进度条
    @Published var isScrubbing = false
    /// 拖动中的位置 0~1
    @Published var scrubRatio: Double = 0
    /// 视频是否为横屏（宽 > 高），用于决定铺满方式
    @Published var isLandscape = false
    /// 长按 1.25 倍速进行中
    @Published var isFast = false

    private var currentVideoID: Int64?
    private var timeObserver: Any?
    private var endObserver: NSObjectProtocol?

    /// 界面显示的进度：拖动时用手指位置，否则用播放进度
    var displayProgress: Double {
        isScrubbing ? scrubRatio : progress
    }

    init() {
        player.actionAtItemEnd = .none
        player.isMuted = false
        configureAudioSession()
        timeObserver = player.addPeriodicTimeObserver(
            forInterval: CMTime(seconds: 0.1, preferredTimescale: 600),
            queue: .main
        ) { [weak self] _ in
            self?.tick()
        }
    }

    deinit {
        if let timeObserver {
            player.removeTimeObserver(timeObserver)
        }
        if let endObserver {
            NotificationCenter.default.removeObserver(endObserver)
        }
    }

    private func configureAudioSession() {
        let session = AVAudioSession.sharedInstance()
        try? session.setCategory(.playback, mode: .moviePlayback, options: [])
        try? session.setActive(true)
    }

    // MARK: - 加载与切换

    func load(id: Int64, url: URL, autoplay: Bool) {
        if currentVideoID == id {
            if autoplay && player.rate == 0 {
                player.play()
                isPlaying = true
            }
            return
        }
        currentVideoID = id
        progress = 0
        duration = 0
        isScrubbing = false
        scrubRatio = 0
        isFast = false
        isLandscape = false

        let item = AVPlayerItem(url: url)
        player.replaceCurrentItem(with: item)
        observeEnd(for: item)
        loadMetadata(for: item)

        if autoplay {
            player.play()
            isPlaying = true
        }
    }

    func pause() {
        player.pause()
        isPlaying = false
    }

    func togglePlay() {
        if player.rate > 0 {
            player.pause()
            isPlaying = false
        } else {
            player.play()
            isPlaying = true
        }
    }

    // MARK: - 长按 1.25 倍速

    func startFast() {
        guard player.rate > 0 else { return }
        player.rate = 1.25
        isFast = true
    }

    func stopFast() {
        guard isFast else { return }
        player.rate = 1.0
        isFast = false
    }

    // MARK: - 进度条拖动

    func beginScrub(_ ratio: Double) {
        isScrubbing = true
        scrubRatio = ratio
    }

    func updateScrub(_ ratio: Double) {
        scrubRatio = ratio
    }

    func endScrub(_ ratio: Double) {
        scrubRatio = ratio
        guard duration > 0 else {
            isScrubbing = false
            return
        }
        let target = CMTime(seconds: ratio * duration, preferredTimescale: 600)
        player.seek(to: target, toleranceBefore: .zero, toleranceAfter: .zero)
        progress = ratio
        isScrubbing = false
    }

    // MARK: - 内部

    private func tick() {
        isPlaying = player.rate > 0
        guard let item = player.currentItem else { return }
        let total = item.duration.seconds
        if total.isFinite && total > 0 {
            duration = total
        }
        if !isScrubbing {
            let current = player.currentTime().seconds
            if current.isFinite {
                progress = duration > 0 ? min(1, max(0, current / duration)) : 0
            }
        }
    }

    private func observeEnd(for item: AVPlayerItem) {
        if let endObserver {
            NotificationCenter.default.removeObserver(endObserver)
        }
        endObserver = NotificationCenter.default.addObserver(
            forName: .AVPlayerItemDidPlayToEndTime,
            object: item,
            queue: .main
        ) { [weak self] _ in
            guard let self else { return }
            self.player.seek(to: .zero)
            self.player.play()
            self.progress = 0
        }
    }

    private func loadMetadata(for item: AVPlayerItem) {
        Task { [weak self] in
            guard let self else { return }
            if let loaded = try? await item.asset.load(.duration) {
                let seconds = loaded.seconds
                if seconds.isFinite && seconds > 0 {
                    await MainActor.run { self.duration = seconds }
                }
            }
            if let tracks = try? await item.asset.loadTracks(withMediaType: .video),
               let track = tracks.first,
               let size = try? await track.load(.naturalSize) {
                await MainActor.run {
                    self.isLandscape = size.width > size.height
                }
            }
        }
    }
}
