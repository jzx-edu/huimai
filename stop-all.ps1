# ============================================================
# 回脉 —— 停止全部服务（Redis / 后端 / 前端）
# ============================================================

# 停止监听相关端口的进程
$ports = 9090, 5174, 6379
foreach ($port in $ports) {
    $conns = Get-NetTCPConnection -State Listen -LocalPort $port -ErrorAction SilentlyContinue
    foreach ($c in $conns) {
        try {
            Stop-Process -Id $c.OwningProcess -Force -ErrorAction Stop
            Write-Host "已停止端口 $port 的进程 (PID $($c.OwningProcess))" -ForegroundColor Green
        } catch {
            Write-Host "端口 $port 进程停止失败（可能需要管理员权限）" -ForegroundColor Yellow
        }
    }
}

# 清理可能残留的 redis-server
Get-Process -Name "redis-server" -ErrorAction SilentlyContinue | ForEach-Object {
    Stop-Process -Id $_.Id -Force
    Write-Host "已停止 redis-server (PID $($_.Id))" -ForegroundColor Green
}

Write-Host "完成。" -ForegroundColor Cyan
