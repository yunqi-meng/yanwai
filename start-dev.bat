@echo off
chcp 65001 >nul
title 言外 - 启动所有服务
echo.
echo ========================================
echo   言外 - 启动服务
echo ========================================
echo.
powershell -ExecutionPolicy Bypass -File "%~dp0start.ps1"
