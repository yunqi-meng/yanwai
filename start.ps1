# ========================================
#  言外 - 听懂TA的潜台词 启动脚本 (Windows)
#  同时启动: 后端 + App前端 + 管理后台
# ========================================

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  言外 - 听懂TA的潜台词" -ForegroundColor Cyan
Write-Host "  启动所有服务 (后端 + App前端 + 管理后台)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# ---------- 释放端口 ----------
Write-Host "[准备] 检查并释放端口..." -ForegroundColor Yellow
$ports = @(8080, 3000, 3001)
foreach ($port in $ports) {
    $conn = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($conn) {
        Stop-Process -Id $conn.OwningProcess -Force -ErrorAction SilentlyContinue
        Write-Host "  已释放端口 $port" -ForegroundColor Gray
    }
}
Start-Sleep -Seconds 1

# ---------- 启动后端 ----------
Write-Host ""
Write-Host "[1/3] 启动后端服务 (端口 8080)..." -ForegroundColor Green
$backendDir = Join-Path $root "yanwai-backend"
if (-not (Test-Path (Join-Path $backendDir "target"))) {
    Write-Host "  首次启动，正在编译后端项目（可能需要几分钟）..." -ForegroundColor Gray
    Push-Location $backendDir
    mvn clean package -DskipTests
    Pop-Location
}
$backendJob = Start-Job -Name "YanwaiBackend" -ScriptBlock {
    param($dir)
    Set-Location $dir
    mvn spring-boot:run 2>&1 | Out-Null
    # 如果已经打包，也可以用下面这行替代上面:
    # java -jar target/yanwai-backend-1.0.0.jar 2>&1 | Out-Null
} -ArgumentList $backendDir
Write-Host "  后端正在启动中..." -ForegroundColor Gray

# ---------- 启动 App 前端 ----------
Write-Host "[2/3] 启动 App 前端 (端口 3000)..." -ForegroundColor Green
$appDir = Join-Path $root "yanwai-frontend"
if (-not (Test-Path (Join-Path $appDir "node_modules"))) {
    Write-Host "  首次启动，正在安装依赖..." -ForegroundColor Gray
    Push-Location $appDir
    npm install
    Pop-Location
}
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$appDir'; Write-Host '言外 App 前端 - http://localhost:3000' -ForegroundColor Cyan; npm run dev" -WindowStyle Normal
Write-Host "  App 前端正在启动..." -ForegroundColor Gray

# ---------- 启动管理后台 ----------
Write-Host "[3/3] 启动管理后台 (端口 3001)..." -ForegroundColor Green
$adminDir = Join-Path $root "yanwai-admin"
if (-not (Test-Path (Join-Path $adminDir "node_modules"))) {
    Write-Host "  首次启动，正在安装依赖..." -ForegroundColor Gray
    Push-Location $adminDir
    npm install
    Pop-Location
}
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$adminDir'; Write-Host '言外 管理后台 - http://localhost:3001' -ForegroundColor Cyan; npm run dev" -WindowStyle Normal
Write-Host "  管理后台正在启动..." -ForegroundColor Gray

# ---------- 等待后端就绪 ----------
Write-Host ""
Write-Host "[等待] 等待后端服务启动..." -ForegroundColor Yellow
$maxWait = 60
$waited = 0
while ($waited -lt $maxWait) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/doc.html" -TimeoutSec 2 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            Write-Host "  后端已就绪! (耗时 ${waited}s)" -ForegroundColor Green
            break
        }
    } catch {}
    Start-Sleep -Seconds 2
    $waited += 2
}
if ($waited -ge $maxWait) {
    Write-Host "  后端启动超时，请检查日志 (预计还需等待片刻)" -ForegroundColor Yellow
}

# ---------- 完成 ----------
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  启动完成!" -ForegroundColor Green
Write-Host ""
Write-Host "  🔮 App 前端:   http://localhost:3000" -ForegroundColor White
Write-Host "  ⚙️  管理后台:   http://localhost:3001" -ForegroundColor White
Write-Host "  📡 后端 API:   http://localhost:8080" -ForegroundColor White
Write-Host "  📖 API 文档:   http://localhost:8080/doc.html" -ForegroundColor White
Write-Host ""
Write-Host "  管理后台默认账号: admin / 123456" -ForegroundColor Gray
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "按任意键关闭所有服务..." -ForegroundColor DarkYellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

# ---------- 清理 ----------
Write-Host ""
Write-Host "正在关闭所有服务..." -ForegroundColor Yellow

# 停止后端 Job
if ($backendJob.State -eq "Running") {
    Stop-Job -Name "YanwaiBackend"
    Remove-Job -Name "YanwaiBackend"
}

# 关闭 Node 进程
Get-Process node -ErrorAction SilentlyContinue | Where-Object {
    $_.MainWindowTitle -match "vite|yanwai" -or $_.CommandLine -match "vite|yanwai"
} | Stop-Process -Force -ErrorAction SilentlyContinue

Write-Host "所有服务已关闭，再见!" -ForegroundColor Green
