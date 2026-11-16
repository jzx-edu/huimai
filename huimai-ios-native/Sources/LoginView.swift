import SwiftUI

/// 登录页：账号密码 → 后端 /auth/login → 保存 JWT
struct LoginView: View {
    @ObservedObject private var session = Session.shared
    @Environment(\.dismiss) private var dismiss

    @State private var username = ""
    @State private var password = ""
    @State private var isSubmitting = false
    @State private var errorText: String?

    var body: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 18) {
                VStack(alignment: .leading, spacing: 6) {
                    Text("回脉")
                        .font(.system(size: 28, weight: .bold))
                        .foregroundStyle(.white)
                    Text("以知识与家庭为核心的短视频社区")
                        .font(.system(size: 13))
                        .foregroundStyle(Color.huimaiMuted)
                }
                .padding(.top, 12)

                VStack(spacing: 12) {
                    TextField("用户名", text: $username)
                        .textInputAutocapitalization(.never)
                        .autocorrectionDisabled()
                        .padding(.horizontal, 14)
                        .frame(height: 46)
                        .background(Color.huimaiCard)
                        .clipShape(RoundedRectangle(cornerRadius: 12))

                    SecureField("密码", text: $password)
                        .textInputAutocapitalization(.never)
                        .autocorrectionDisabled()
                        .padding(.horizontal, 14)
                        .frame(height: 46)
                        .background(Color.huimaiCard)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                }

                if let errorText {
                    Text(errorText)
                        .font(.system(size: 13))
                        .foregroundStyle(Color(red: 0.91, green: 0.33, blue: 0.29))
                }

                Button {
                    Task { await submit() }
                } label: {
                    Text(isSubmitting ? "登录中…" : "登录")
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundStyle(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 46)
                        .background(Color.huimaiPrimary)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                }
                .disabled(isSubmitting || username.isEmpty || password.isEmpty)
                .opacity(isSubmitting || username.isEmpty || password.isEmpty ? 0.6 : 1)

                Text("服务器：\(AppConfig.apiBase)")
                    .font(.system(size: 11))
                    .foregroundStyle(Color.huimaiMuted)

                Spacer()
            }
            .padding(.horizontal, 20)
            .background(Color.huimaiBackground.ignoresSafeArea())
            .foregroundStyle(.white)
            .navigationTitle("登录")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("关闭") { dismiss() }
                        .foregroundStyle(Color.huimaiMuted)
                }
            }
        }
    }

    private func submit() async {
        isSubmitting = true
        errorText = nil
        defer { isSubmitting = false }
        do {
            let result: LoginResult = try await APIClient.shared.post(
                "/auth/login",
                body: ["username": username, "password": password]
            )
            session.save(result)
            dismiss()
        } catch {
            errorText = (error as? LocalizedError)?.errorDescription ?? "登录失败"
        }
    }
}
