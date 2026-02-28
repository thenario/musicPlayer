@echo off
chcp 936 > nul
title Flask音乐播放器启动脚本

echo ================================
echo    Flask音乐播放器启动脚本
echo ================================

REM 确保在脚本所在目录运行
cd /d "%~dp0"

REM 检查启动文件是否存在
if not exist "app_start.py" (
    echo 错误：当前目录下没有app_start.py文件
    dir *.py
    pause
    exit /b 1
)

echo.
echo [1] 检查Python环境
python --version

echo.
echo [2] 激活虚拟环境
if exist ".venv\Scripts\activate.bat" (
    call ".venv\Scripts\activate.bat"
    echo 已激活虚拟环境 (.venv)
) else if exist "venv\Scripts\activate.bat" (
    call "venv\Scripts\activate.bat"
    echo 已激活虚拟环境 (venv)
) else (
    echo 警告：未找到虚拟环境，使用系统Python
)

echo.
echo [3] 检查Flask依赖
pip show flask >nul 2>&1
if errorlevel 1 (
    echo 安装Flask...
    pip install flask
) else (
    echo Flask已安装
)

echo.
echo [4] 启动Flask音乐播放器
echo ================================
echo.
echo 访问地址：http://127.0.0.1:5000
echo.
echo 调试模式已启用
echo Debugger PIN: 292-758-499
echo.
echo 按 Ctrl+C 停止服务器
echo ================================
echo.

REM 设置环境变量
set FLASK_APP=app_start.py
set FLASK_ENV=development
set FLASK_DEBUG=1

REM 运行Flask应用
python app_start.py

echo.
echo ================================
echo Flask应用已停止
pause