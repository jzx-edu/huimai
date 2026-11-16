import SwiftUI

/// 底部进度条：2px 细线 + 可点击定位 + 可拖动 seek
/// 外层 28pt 热区，拖动时圆点 8pt 放大到 13pt
struct ProgressScrubber: View {
    @ObservedObject var player: PlayerController

    var body: some View {
        GeometryReader { proxy in
            let width = max(proxy.size.width, 1)
            let ratio = min(1, max(0, player.displayProgress))

            ZStack(alignment: .leading) {
                Capsule()
                    .fill(Color.white.opacity(0.18))
                    .frame(height: 2)

                Capsule()
                    .fill(Color.white)
                    .frame(width: max(2, width * ratio), height: 2)

                Circle()
                    .fill(Color.white)
                    .frame(width: player.isScrubbing ? 13 : 8, height: player.isScrubbing ? 13 : 8)
                    .offset(x: width * ratio - (player.isScrubbing ? 6.5 : 4))
                    .shadow(color: .black.opacity(0.4), radius: 1, y: 1)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)
            .contentShape(Rectangle())
            .gesture(
                DragGesture(minimumDistance: 0)
                    .onChanged { value in
                        let position = min(1, max(0, value.location.x / width))
                        if player.isScrubbing {
                            player.updateScrub(position)
                        } else {
                            player.beginScrub(position)
                        }
                    }
                    .onEnded { value in
                        let position = min(1, max(0, value.location.x / width))
                        player.endScrub(position)
                    }
            )
        }
        .frame(height: 28)
    }
}
