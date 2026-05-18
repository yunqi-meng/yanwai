#!/bin/bash

echo "========================================="
echo "  言外 - 听懂TA的潜台词 启动脚本"
echo "========================================="

# 启动后端
echo ""
echo "[1/2] 正在启动后端服务..."
cd "$(dirname "$0")/yanwai-backend"
if [ ! -d "target" ]; then
    echo "正在编译后端项目..."
    mvn clean package -DskipTests
fi
echo "后端服务将在 http://localhost:8080 启动"

# 启动前端
echo ""
echo "[2/2] 正在启动前端服务..."
cd "$(dirname "$0")/yanwai-frontend"
if [ ! -d "node_modules" ]; then
    echo "正在安装前端依赖..."
    npm install
fi
echo "前端服务将在 http://localhost:3000 启动"
echo ""

echo "========================================="
echo "  启动完成！"
echo "  前端: http://localhost:3000"
echo "  后端: http://localhost:8080"
echo "========================================="
