@echo off
chcp 65001 >nul
title Vue Music Player 开发服务器

echo ========================================
echo   正在启动 Vue Music Player 开发服务器
echo ========================================
echo.
echo 注意：此窗口将保持运行状态
echo 要停止服务器，请按 Ctrl+C
echo.
echo 启动时间：%date% %time%
echo ========================================
echo.

REM 切换到Z盘
Z:

REM 进入项目目录
cd /d Z:\vue3_projects\vue_musicplayer

if errorlevel 1 (
    echo 错误: 无法进入目录 Z:\vue3_projects\vue_musicplayer
    echo 请确保该目录存在
    pause
    exit /b 1
)

echo ✓ 已切换到项目目录: %cd%
echo.

REM 执行npm run dev
echo 正在启动开发服务器...
echo ========================================
echo.

npm run dev

echo.
echo ========================================
echo 开发服务器已停止
echo.
pause