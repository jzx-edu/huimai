# ============================================================
# 回脉 —— 一键启动 Redis + 后端 + 前端
# 用法：在本目录右键“使用 PowerShell 运行”，或执行 .\start-all.ps1
# 说明：中文路径下 spring-boot:run 会因 classpath 编码问题失败，
#       故后端采用“先打包再 java -jar”的方式运行。
# ============================================================

# ---- 路径配置（如环境变化，只需修改这里）----
$RedisServer = "D:\career(1)\Redis-x64-7.2.16\Redis-7.2.16-Windows-x64-msys2\redis-server.exe"
$JavaHome    = "D:\career(1)\jdk-17.0.20.1+1"
$JavaExe     = Join-Path $JavaHome "bin\java.exe"
$MvnCmd      = "D:\career\apache-maven-3.9.16\bin\mvn.cmd"
$RootDir     = "D:\回脉"
$BackendDir  = Join-Path $RootDir "huimai-backend"
$AppDir      = Join-Path $RootDir "huimai-frontend"
$JarPath     = Join-Path $BackendDir "target\huimai-backend-0.1.0.jar"

function Test-Port($port) {
    return [bool](Get-NetTCPConnection -State Listen -LocalPort $port -ErrorAction SilentlyContinue)
}

# 1. 启动 Redis
if (Test-Port 6379) {
    Write-Host "[1/3] Redis 已在运行" -ForegroundColor Green
} else {
    Write-Host "[1/3] 启动 Redis ..." -ForegroundColor Yellow
    Start-Process -FilePath $RedisServer -WindowStyle Hidden
    Start-Sleep -Seconds 2
}

# 2. 启动后端（java -jar；jar 不存在则先打包）
if (Test-Port 9090) {
    Write-Host "[2/3] 后端端口 9090 已占用，跳过启动" -ForegroundColor Green
} else {
    if (-not (Test-Path -LiteralPath $JarPath)) {
        Write-Host "[2/3] 未找到可执行 jar，先执行打包 ..." -ForegroundColor Yellow
        $env:JAVA_HOME = $JavaHome
        & $MvnCmd -f "$BackendDir\pom.xml" package -DskipTests
    }
    Write-Host "[2/3] 启动后端（java -jar，端口 9090）..." -ForegroundColor Yellow
    $backendCmd = "`$env:JAVA_HOME='$JavaHome'; & '$JavaExe' -jar '$JarPath'"
    Start-Process powershell -ArgumentList "-NoExit", "-Command", $backendCmd
}

# 3. 启动前端
if (Test-Port 5174) {
    Write-Host "[3/3] 前端端口 5174 已占用，跳过启动" -ForegroundColor Green
} else {
    Write-Host "[3/3] 启动前端（Vite，端口 5174）..." -ForegroundColor Yellow
    $frontendCmd = "Set-Location '$AppDir'; npm run dev"
    Start-Process powershell -ArgumentList "-NoExit", "-Command", $frontendCmd
}

Write-Host ""
Write-Host "全部服务已发起，请等待两个新窗口完成启动。" -ForegroundColor Cyan
Write-Host "  前端地址 : http://localhost:5174/" -ForegroundColor White
Write-Host "  后端接口 : http://localhost:9090/api" -ForegroundColor White
Write-Host "  接口文档 : http://localhost:9090/api/doc.html" -ForegroundColor White
Write-Host "  演示账号 : test/123456 ， admin/admin123" -ForegroundColor White
