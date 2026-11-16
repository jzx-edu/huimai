import SwiftUI

/// 单个视频页：激活时渲染播放器图层，非激活时渲染封面
struct VideoPageView: View {
    let video: VideoItem
    @ObservedObject var player: PlayerController
    let isActive: Bool

    var body: some View {
        ZStack {
            Color.black

            if isActive {
                PlayerLayerView(player: player.player, isLandscape: player.isLandscape)
            } else if let cover = AppConfig.mediaURL(video.coverUrl) {
                AsyncImage(url: cover) { image in
                    image
                        .resizable()
                        .scaledToFill()
                } placeholder: {
                    Color.black
                }
            }

            // 暂停遮罩：只显示在当前激活视频上
            if isActive && !player.isPlaying {
                Image(systemName: "play.fill")
                    .font(.system(size: 30, weight: .semibold))
                    .foregroundStyle(.white)
                    .frame(width: 64, height: 64)
                    .background(Color.black.opacity(0.4))
                    .clipShape(Circle())
            }

            // 长按倍速提示
            if isActive && player.isFast {
                HStack(spacing: 7) {
                    Image(systemName: "forward.fill")
                        .font(.system(size: 15, weight: .semibold))
                    Text("1.25 倍速播放中")
                        .font(.system(size: 14, weight: .medium))
                }
                .foregroundStyle(.white)
                .padding(.horizontal, 16)
                .padding(.vertical, 10)
                .background(Color.black.opacity(0.66))
                .clipShape(Capsule())
            }
        }
        .clipped()
    }
}

/// 原生信息流：整屏一页、上下滑动切换
struct FeedView: View {
    @ObservedObject var vm: FeedViewModel

    var body: some View {
        GeometryReader { proxy in
            let pageHeight = proxy.size.height + proxy.safeAreaInsets.top + proxy.safeAreaInsets.bottom

            ScrollView(.vertical) {
                LazyVStack(spacing: 0) {
                    ForEach(vm.videos) { video in
                        VideoPageView(
                            video: video,
                            player: vm.player,
                            isActive: video.id == vm.activeID
                        )
                        .frame(width: proxy.size.width, height: pageHeight)
                        .id(video.id)
                        .onTapGesture {
                            vm.handleTap()
                        }
                        .onLongPressGesture(minimumDuration: 0.5, maximumDistance: 12) {
                            vm.beginLongPress()
                        } onPressingChanged: { pressing in
                            if !pressing {
                                vm.endLongPress()
                            }
                        }
                    }
                }
                .scrollTargetLayout()
            }
            .scrollIndicators(.hidden)
            .scrollTargetBehavior(.paging)
            .scrollPosition(id: $vm.activeID)
            .ignoresSafeArea()
            .onChange(of: vm.activeID) { _, newValue in
                vm.syncPlayer()
                if let newValue {
                    vm.loadMoreIfNeeded(currentID: newValue)
                }
            }
            .overlay(alignment: .center) {
                if vm.videos.isEmpty {
                    VStack(spacing: 10) {
                        if vm.isLoading {
                            ProgressView()
                                .tint(.white)
                            Text("加载中…")
                                .font(.system(size: 14))
                                .foregroundStyle(.white.opacity(0.8))
                        } else {
                            Text(vm.errorText ?? "暂无视频")
                                .font(.system(size: 14))
                                .foregroundStyle(.white.opacity(0.8))
                                .multilineTextAlignment(.center)
                                .padding(.horizontal, 32)
                            Button("重新加载") {
                                Task { await vm.loadMore() }
                            }
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundStyle(.white)
                        }
                    }
                }
            }
        }
    }
}
