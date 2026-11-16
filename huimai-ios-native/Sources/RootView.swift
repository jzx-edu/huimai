import SwiftUI

enum AppTab: Hashable {
    case feed
    case discover
    case shop
    case profile
}

// MARK: - 主题色

extension Color {
    static let huimaiBackground = Color(red: 0.043, green: 0.055, blue: 0.086)
    static let huimaiPrimary = Color(red: 0.145, green: 0.388, blue: 0.922)
    static let huimaiMuted = Color(red: 0.541, green: 0.580, blue: 0.651)
    static let huimaiCard = Color(red: 0.086, green: 0.110, blue: 0.157)
}

struct RootView: View {
    @StateObject private var feed = FeedViewModel()
    @ObservedObject private var session = Session.shared

    @State private var tab: AppTab = .feed
    @State private var showLogin = false
    @State private var showActionSheet = false

    var body: some View {
        GeometryReader { proxy in
            let bottomSafe = max(proxy.safeAreaInsets.bottom, 0)

            ZStack(alignment: .bottom) {
                Color.black.ignoresSafeArea()

                content

                VStack(spacing: 0) {
                    if tab == .feed {
                        ProgressScrubber(player: feed.player)
                    }
                    bottomBar(bottomSafe: bottomSafe)
                }
            }
        }
        .background(Color.black.ignoresSafeArea())
        .task {
            await feed.loadFirstIfNeeded()
        }
        .sheet(isPresented: $showLogin) {
            LoginView()
        }
        .sheet(isPresented: $showActionSheet) {
            ActionSheetView(feed: feed)
        }
    }

    @ViewBuilder
    private var content: some View {
        switch tab {
        case .feed:
            FeedView(vm: feed)
        case .discover:
            PlaceholderPage(title: "发现", subtitle: "搜索与分类浏览将在第二阶段迁移到原生实现")
        case .shop:
            PlaceholderPage(title: "商城", subtitle: "商品浏览与下单将在第二阶段迁移到原生实现")
        case .profile:
            ProfilePage(showLogin: $showLogin)
        }
    }

    private func bottomBar(bottomSafe: CGFloat) -> some View {
        ZStack(alignment: .bottom) {
            if tab == .feed {
                LinearGradient(
                    colors: [
                        Color.black.opacity(0.62),
                        Color.black.opacity(0.3),
                        Color.black.opacity(0)
                    ],
                    startPoint: .bottom,
                    endPoint: .top
                )
                .frame(height: 120 + bottomSafe)
                .allowsHitTesting(false)
            } else {
                Color.huimaiCard
                    .opacity(0.98)
                    .frame(height: 52 + bottomSafe)
                    .overlay(alignment: .top) {
                        Rectangle()
                            .fill(Color.white.opacity(0.06))
                            .frame(height: 0.5)
                    }
            }

            HStack(spacing: 0) {
                tabButton(.feed, systemImage: "house")
                tabButton(.discover, systemImage: "square.grid.2x2")
                tabButton(.shop, systemImage: "bag")
                tabButton(.profile, systemImage: "person")
            }
            .frame(height: 52)
            .padding(.bottom, bottomSafe)

            // 视频互动悬浮按钮：居中夹在 4 个图标中间，仅在播放时点亮
            if tab == .feed && feed.player.isPlaying {
                Button {
                    feed.pause()
                    showActionSheet = true
                } label: {
                    Image(systemName: "ellipsis.message.fill")
                        .font(.system(size: 19, weight: .semibold))
                        .foregroundStyle(.white)
                        .frame(width: 42, height: 42)
                        .background(
                            LinearGradient(
                                colors: [Color(red: 0.23, green: 0.51, blue: 0.96), Color.huimaiPrimary],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .clipShape(Circle())
                        .shadow(color: Color.huimaiPrimary.opacity(0.55), radius: 8, y: 3)
                }
                .buttonStyle(.plain)
                .padding(.bottom, bottomSafe + 5)
            }
        }
    }

    private func tabButton(_ target: AppTab, systemImage: String) -> some View {
        Button {
            if tab != target {
                if tab == .feed {
                    feed.pause()
                }
                tab = target
            }
        } label: {
            Image(systemName: systemImage)
                .font(.system(size: 22, weight: .regular))
                .foregroundStyle(tab == target ? Color.white : Color.huimaiMuted)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }
}

// MARK: - 第二阶段占位页

struct PlaceholderPage: View {
    let title: String
    let subtitle: String

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            Text(title)
                .font(.system(size: 24, weight: .bold))
                .foregroundStyle(.white)

            Text(subtitle)
                .font(.system(size: 14))
                .foregroundStyle(Color.huimaiMuted)
                .lineSpacing(4)

            Spacer()
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.horizontal, 20)
        .padding(.top, 24)
        .background(Color.huimaiBackground.ignoresSafeArea())
    }
}

#Preview {
    RootView()
}
