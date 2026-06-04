#!/bin/bash

# ========================================
#  言外 - 听懂TA的潜台词 启动脚本 (Linux/Mac)
#  同时启动: 后端 + App前端 + 管理后台
# ========================================

ROOT="$(cd "$(dirname "$0")" && pwd)"

echo ""
echo "========================================"
echo "  言外 - 听懂TA的潜台词"
echo "  启动所有服务"
echo "========================================"

# ---------- 释放端口 ----------
echo ""
echo "[准备] 检查并释放端口..."
for port in 8080 3000 3001; do
    PID=$(lsof -ti:$port 2>/dev/null)
    if [ -n "$PID" ]; then
        kill -9 $PID 2>/dev/null
        echo "  已释放端口 $port"
    fi
done
sleep 1

# ---------- 启动后端 ----------
echo ""
echo "[1/3] 启动后端服务 (端口 8080)..."
cd "$ROOT/yanwai-backend"
if [ ! -d "target" ]; then
    echo "  首次启动，正在编译后端项目..."
    mvn clean package -DskipTests
fi
mvn spring-boot:run &
BACKEND_PID=$!
echo "  后端 PID: $BACKEND_PID"

# ---------- 启动 App 前端 ----------
echo ""
echo "[2/3] 启动 App 前端 (端口 3000)..."
cd "$ROOT/yanwai-frontend"
if [ ! -d "node_modules" ]; then
    echo "  正在安装依赖..."
    npm install
fi
npm run dev &
APP_PID=$!
echo "  App 前端 PID: $APP_PID"

# ---------- 启动管理后台 ----------
echo ""
echo "[3/3] 启动管理后台 (端口 3001)..."
cd "$ROOT/yanwai-admin"
if [ ! -d "node_modules" ]; then
    echo "  正在安装依赖..."
    npm install
fi
npm run dev &
ADMIN_PID=$!
echo "  管理后台 PID: $ADMIN_PID"

# ---------- 等待后端就绪 ----------
echo ""
echo "[等待] 等待后端服务启动..."
for i in $(seq 1 30); do
    if curl -s http://localhost:8080/doc.html > /dev/null 2>&1; then
        echo "  后端已就绪! (耗时 $((i*2))s)"
        break
    fi
    sleep 2
done

# ---------- 完成 ----------
echo ""
echo "========================================"
echo "  启动完成!"
echo ""
echo "  🔮 App 前端:   http://localhost:3000"
echo "  ⚙️  管理后台:   http://localhost:3001"
echo "  📡 后端 API:   http://localhost:8080"
echo "  📖 API 文档:   http://localhost:8080/doc.html"
echo ""
echo "  管理后台默认账号: admin / 123456"
echo "========================================"
echo ""
echo "按 Ctrl+C 停止所有服务"

# 捕获退出信号
trap "echo ''; echo '正在关闭所有服务...'; kill $BACKEND_PID $APP_PID $ADMIN_PID 2>/dev/null; echo '已关闭'; exit 0" INT TERM

# 等待
wait
