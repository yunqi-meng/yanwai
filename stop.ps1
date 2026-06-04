# ========================================
#  言外 - 停止所有服务
# ========================================

Write-Host "正在关闭所有服务..." -ForegroundColor Yellow

# 关闭端口 8080 (后端 Java)
$conn8080 = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | Select-Object -First 1
if ($conn8080) {
    Stop-Process -Id $conn8080.OwningProcess -Force -ErrorAction SilentlyContinue
    Write-Host "  已停止后端 (8080)" -ForegroundColor Gray
}

# 关闭端口 3000 (App 前端)
$conn3000 = Get-NetTCPConnection -LocalPort 3000 -ErrorAction SilentlyContinue | Select-Object -First 1
if ($conn3000) {
    Stop-Process -Id $conn3000.OwningProcess -Force -ErrorAction SilentlyContinue
    Write-Host "  已停止 App 前端 (3000)" -ForegroundColor Gray
}

# 关闭端口 3001 (管理后台)
$conn3001 = Get-NetTCPConnection -LocalPort 3001 -ErrorAction SilentlyContinue | Select-Object -First 1
if ($conn3001) {
    Stop-Process -Id $conn3001.OwningProcess -Force -ErrorAction SilentlyContinue
    Write-Host "  已停止管理后台 (3001)" -ForegroundColor Gray
}

Write-Host "所有服务已关闭!" -ForegroundColor Green
