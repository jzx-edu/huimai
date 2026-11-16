# 回脉 iOS 云端编译指南（Windows 无 Mac 方案）

> 适用情况：你在 Windows 上开发，没有 Mac，但想得到一个能在 iPhone 安装、能上架 App Store 的"真正 App"。
> 核心思路：用 **Capacitor** 把现有 Vue 网页包装成原生 iOS 工程（已在本地完成），再用云端 Mac 服务 **Codemagic** 完成编译和签名。

---

## 一、现在已经完成了什么

我已经在你的电脑上完成了所有"不需要 Mac"的改造：

1. 安装 Capacitor 8，生成了完整的 Xcode 原生工程，目录在 `huimai-frontend/ios`
2. 应用标识 `com.huimai.app`，应用名"回脉"
3. 把接口地址、视频/封面/头像等媒体地址改成了"打包后可直连后端"的绝对地址（开发环境仍走原来的代理，互不影响）
4. 配置了原生状态栏（深色背景白字）、刘海/灵动岛安全区避让
5. 后端 9090 已放行防火墙，并验证了对 App 来源（`capacitor://localhost`）的跨域 CORS 完全正常
6. 已写好云端编译配置 `codemagic.yaml` 和 `.gitignore`

**为什么还差一步**：苹果规定，最终的 `.ipa` 安装包必须在 Mac + Xcode 上编译、并用苹果证书签名。这一步你的 Windows 做不了，所以交给云端 Mac。

---

## 二、需要准备的 3 个账号

| 账号 | 费用 | 是否必须 | 作用 |
|---|---|---|---|
| Apple Developer Program | 99 美元/年 | **必须** | 提供签名证书，没有它无法真机安装、无法上架 |
| GitHub | 免费 | 推荐 | 存放代码，让云端能拉取 |
| Codemagic | 有免费额度（每月 500 分钟） | 必须 | 云端 Mac 编译，本项目一次构建约 10–15 分钟 |

> 先去苹果官网注册 Apple Developer Program：https://developer.apple.com/programs/ ，用你的 Apple ID 申请，通常 1–2 天内审核通过。注册时记下你的 **Apple ID 账号密码**，后面签名要用。

---

## 三、把代码上传到 GitHub（二选一）

### 方案 A：GitHub Desktop（图形化，推荐新手）

1. 下载安装 GitHub Desktop：https://desktop.github.com （它自带 Git，无需另装）
2. 打开后用 GitHub 账号登录
3. 菜单 **File → Add Local Repository**，选择 `D:\回脉`
   - 它会提示这不是 Git 仓库，点 **"create a repository"**（创建仓库），名字随意，保持默认即可
4. 左下角确认要提交的文件（`.gitignore` 已自动排除 node_modules 等大文件），在 **Summary** 随便填如"init"，点 **Commit to main**
5. 点顶部 **Publish repository**，**取消勾选 "Keep this code private" 可按需**，建议先设为 Private（私有），然后点 Publish
6. 完成后你的 GitHub 上就有这个项目了

### 方案 B：命令行 Git

1. 下载安装 Git for Windows：https://git-scm.com/download/win ，一路下一步
2. 重新打开 PowerShell，执行（把下面的地址换成你在 GitHub 新建的空仓库地址）：

```powershell
cd D:\回脉
git init
git add .
git commit -m "init"
git branch -M main
git remote add origin https://github.com/你的用户名/你的仓库名.git
git push -u origin main
```

---

## 四、在 Codemagic 上构建（云端 Mac）

1. 打开 https://codemagic.io ，用 **GitHub 账号登录**（Sign up with GitHub）
2. 首次登录会让你授权访问 GitHub，授权后它会列出你的仓库，找到回脉项目，点 **Add application**
3. 它会自动识别仓库根目录里的 `codemagic.yaml`，识别出工作流 **"回脉 iOS"**

### 配置 iOS 自动签名（最关键的一步）

1. 在 Codemagic 左侧进入 **Teams → 你的团队 → Code signing**（或在应用页面找 **Code signing** 标签）
2. 找到 **iOS certificates**，点 **Add certificate**，选择 **Apple Developer Portal** 方式
3. 输入你注册开发者账号用的 **Apple ID 和 App 专用密码**（App 专用密码在 https://appleid.apple.com → "App 专用密码"里生成）
4. Codemagic 会自动连接苹果后台，帮你创建/导入 **Distribution Certificate（发布证书）**
5. 再在 **Provisioning profiles** 里点 **Fetch**，它会自动为 `com.huimai.app` 创建描述文件
6. 确认证书和描述文件前面都打上勾

### 启动构建

1. 回到应用主页面，选中工作流 **"回脉 iOS"**，点 **Start new build**
2. 等待约 10–15 分钟，可实时看到云端 Mac 的日志
3. 构建成功后，页面 **Artifacts** 里会出现 `.ipa` 文件；同时 `codemagic.yaml` 里配置的邮箱也会收到下载链接（记得把 yaml 里的 `your-email@example.com` 改成你自己的邮箱）

---

## 五、把 App 装到自己的 iPhone

拿到 `.ipa` 后，普通方式不能直接双击安装（苹果的限制），有两种简单办法：

- **办法一（推荐，最省事）**：在 Codemagic 构建成功页面，直接用 iPhone 扫描它提供的 **二维码安装**，手机会弹出"是否安装"，确认即可（需联网，且证书已包含你的设备）
- **办法二**：用免费工具如 **AltStore**（https://altstore.io ）或 **Sideloadly**，把 ipa 通过数据线装到手机
- 首次打开若提示"未受信任的开发者"，到 iPhone **设置 → 通用 → VPN与设备管理**，信任你的开发者证书即可

> 注意：用开发者证书签名的 App，真机安装有效期通常为 1 年；正式上架 App Store 后用户安装则无此限制。

---

## 六、上架 App Store（可选，想给别人下载时再做）

1. 在 App Store Connect（https://appstoreconnect.apple.com ）新建 App，填写名称、分类、截图、隐私政策等
2. Codemagic 构建时可增加 **publishing → App Store Connect** 步骤，自动上传，或用 **Transporter** 工具把 ipa 上传
3. 提交审核，苹果审核通过后即可在 App Store 搜索下载

---

## 七、关于后端地址的重要提醒（务必看）

App 里的视频、登录、商城等数据都来自你的后端，这是和网页版最大的区别：

1. **当前 `.env.production` 里填的是局域网地址** `http://192.168.1.7:9090/api`，只在你手机和电脑连同一个 WiFi 时能用；而且电脑的局域网 IP 是 DHCP 自动分配的，**可能变化**，变化后用 `ipconfig` 查到新 IP，改 `.env.production` 后重新构建
2. **要让 App 在任何网络下都能用、或通过审核上架，必须把后端部署到公网**（云服务器），并使用 **HTTPS 域名**，例如 `https://api.yourdomain.com/api`，然后：
   - 把该地址填进 `.env.production`
   - 此时可删除 `ios/App/App/Info.plist` 里那段 `NSAppTransportSecurity`（HTTP 明文例外），因为 HTTPS 不再需要
3. 每次改完前端代码，重新执行下面两条命令即可让云端拿到最新版（再 push 到 GitHub 后在 Codemagic 重新构建）：

```powershell
cd D:\回脉\huimai-frontend
npm run build
npx cap sync ios
```

---

## 附：当前关键信息速查

- 应用标识 Bundle ID：`com.huimai.app`
- Xcode 工程：`huimai-frontend/ios/App/App.xcodeproj`，Scheme 名：`App`
- 云端配置：根目录 `codemagic.yaml`
- 后端地址配置：`huimai-frontend/.env.production`
- 本地网页测试：`http://192.168.1.7:5174`
