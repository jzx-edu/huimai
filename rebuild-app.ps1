# 一键重新构建前端并同步到 iOS 原生工程（更新 App 前在本地跑这个）
# 用法：在 PowerShell 里执行  D:\回脉\rebuild-app.ps1
Set-Location "D:\回脉\huimai-frontend"

Write-Host "==> 1/2 构建 Web 资源（读取 .env.production）" -ForegroundColor Cyan
npm run build
if ($LASTEXITCODE -ne 0) { Write-Host "构建失败，请检查上面的报错" -ForegroundColor Red; exit 1 }

Write-Host "==> 2/2 同步到 iOS 原生工程" -ForegroundColor Cyan
npx cap sync ios

Write-Host ""
Write-Host "本地构建与同步完成。接下来三步：" -ForegroundColor Green
Write-Host "  1) 用 GitHub Desktop 提交并 Push（Commit -> Push origin）"
Write-Host "  2) Codemagic 自动/手动重新构建，下载新的 huimai-unsigned.ipa"
Write-Host "  3) Sideloadly 重新拖入新 ipa -> Start 覆盖安装（数据保留）"
