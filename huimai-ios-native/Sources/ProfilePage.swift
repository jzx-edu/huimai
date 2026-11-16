import SwiftUI

/// 「我的」页（第一阶段）：登录态、服务器地址、原生迁移进度说明
struct ProfilePage: View {
    @Binding var showLogin: Bool
    @ObservedObject private var session = Session.shared
    @AppStorage("huimai.apiBase") private var apiBase = "http://192.168.1.7:9090/api"

    @State private var draftBase = ""
    @State private var saved = false

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                HStack(spacing: 14) {
                    ZStack {
                        Circle()
                            .fill(Color.huimaiPrimary.opacity(0.16))
                        Text(initial)
                            .font(.system(size: 20, weight: .semibold))
                            .foregroundStyle(Color.huimaiPrimary)
                    }
                    .frame(width: 60, height: 60)

                    VStack(alignment: .leading, spacing: 4) {
                        Text(session.isLoggedIn ? (session.nickname ?? "回脉用户") : "未登录")
                            .font(.system(size: 17, weight: .semibold))
                            .foregroundStyle(.white)
                        Text(session.isLoggedIn ? "已登录，可点赞、收藏、评论" : "登录后可使用互动功能")
                            .font(.system(size: 12))
                            .foregroundStyle(Color.huimaiMuted)
                    }

                    Spacer()

                    Button {
                        if session.isLoggedIn {
                            session.clear()
                        } else {
                            showLogin = true
                        }
                    } label: {
                        Text(session.isLoggedIn ? "退出" : "登录")
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundStyle(session.isLoggedIn ? Color.huimaiMuted : .white)
                            .padding(.horizontal, 16)
                            .frame(height: 32)
                            .background(session.isLoggedIn ? Color.huimaiCard : Color.huimaiPrimary)
                            .clipShape(Capsule())
                    }
                    .buttonStyle(.plain)
                }

                VStack(alignment: .leading, spacing: 10) {
                    Text("服务器地址")
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundStyle(Color.huimaiMuted)

                    HStack(spacing: 8) {
                        TextField("http://192.168.1.7:9090/api", text: $draftBase)
                            .textInputAutocapitalization(.never)
                            .autocorrectionDisabled()
                            .keyboardType(.URL)
                            .font(.system(size: 13))
                            .foregroundStyle(.white)
                            .padding(.horizontal, 12)
                            .frame(height: 40)
                            .background(Color.huimaiCard)
                            .clipShape(RoundedRectangle(cornerRadius: 10))

                        Button("保存") {
                            let trimmed = draftBase.trimmingCharacters(in: .whitespacesAndNewlines)
                            if !trimmed.isEmpty {
                                apiBase = trimmed
                                saved = true
                            }
                        }
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundStyle(.white)
                        .padding(.horizontal, 14)
                        .frame(height: 40)
                        .background(Color.huimaiPrimary)
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                        .buttonStyle(.plain)
                    }

                    Text(saved ? "已保存，重新打开 App 生效" : "电脑 IP 变化后在这里改成新地址即可")
                        .font(.system(size: 11))
                        .foregroundStyle(Color.huimaiMuted)
                }
                .padding(14)
                .background(Color.huimaiCard.opacity(0.6))
                .clipShape(RoundedRectangle(cornerRadius: 14))

                VStack(alignment: .leading, spacing: 8) {
                    Text("原生重写进度")
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundStyle(Color.huimaiMuted)
                    Text("第一阶段：视频信息流 + 播放器（全屏无白边、进度条可拖动、长按 1.25 倍速）")
                    Text("第二阶段：评论、点赞收藏、私信、通知")
                    Text("第三阶段：商城、发布、作者主页")
                }
                .font(.system(size: 12))
                .foregroundStyle(.white.opacity(0.85))
                .lineSpacing(4)
                .padding(14)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color.huimaiCard.opacity(0.6))
                .clipShape(RoundedRectangle(cornerRadius: 14))

                Spacer(minLength: 24)
            }
            .padding(.horizontal, 20)
            .padding(.top, 16)
        }
        .scrollIndicators(.hidden)
        .background(Color.huimaiBackground.ignoresSafeArea())
        .onAppear {
            draftBase = apiBase
        }
    }

    private var initial: String {
        let name = session.nickname ?? "回"
        return String(name.prefix(1))
    }
}
