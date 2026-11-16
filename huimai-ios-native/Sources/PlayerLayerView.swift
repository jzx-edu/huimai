import AVFoundation
import SwiftUI

final class PlayerUIView: UIView {
    override class var layerClass: AnyClass { AVPlayerLayer.self }

    var playerLayer: AVPlayerLayer {
        layer as! AVPlayerLayer
    }
}

/// 把 AVPlayer 的图层嵌进 SwiftUI
/// 竖屏视频用 resizeAspectFill 铺满（不会出现白边/黑边）
/// 横屏视频用 resizeAspect 让画面按宽度铺满、上下留黑，符合之前确定的「横向直角卡片」观感
struct PlayerLayerView: UIViewRepresentable {
    let player: AVPlayer
    let isLandscape: Bool

    func makeUIView(context: Context) -> PlayerUIView {
        let view = PlayerUIView()
        view.backgroundColor = .black
        view.playerLayer.player = player
        view.playerLayer.videoGravity = isLandscape ? .resizeAspect : .resizeAspectFill
        return view
    }

    func updateUIView(_ uiView: PlayerUIView, context: Context) {
        if uiView.playerLayer.player !== player {
            uiView.playerLayer.player = player
        }
        let gravity: AVLayerVideoGravity = isLandscape ? .resizeAspect : .resizeAspectFill
        if uiView.playerLayer.videoGravity != gravity {
            uiView.playerLayer.videoGravity = gravity
        }
    }
}
