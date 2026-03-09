@echo off
chcp 936 > nul
title Flask 音乐播放器启动器

echo ========================================
echo       Flask 音乐播放器启动脚本
echo ========================================

REM [0] 确保在脚本所在目录运行
cd /d "%~dp0"

REM [1] 检查核心启动文件
if not exist "app_start.py" (
    echo [错误] 当前目录下找不到 app_start.py
    dir *.py
    pause
    exit /b 1
)

echo [1] 检查 Python 环境:
python --version

echo.
echo [2] 激活虚拟环境:
SET VENV_PATH=%~dp0.venv
if exist "%VENV_PATH%\Scripts\activate.bat" (
    call "%VENV_PATH%\Scripts\activate.bat"
    echo 已成功切换至虚拟环境 (.venv)
) else (
    echo [警告] 未找到虚拟环境目录，将使用系统 Python 运行
)

echo.
echo [3] 检查并同步依赖库:
REM 定义需要检查的库列表
set LIBRARIES=flask flask-cors flask-limiter flask-login mutagen redis flask-sqlalchemy PyJWT

for %%i in (%LIBRARIES%) do (
    pip show %%i >nul 2>&1
    if errorlevel 1 (
        echo 正在安装缺失的库: %%i ...
        pip install %%i
    ) else (
        echo [已就绪] %%i
    )
)

echo.
echo [4] 准备启动服务...
echo ========================================
echo 访问地址：http://127.0.0.1:5000
echo 调试模式：已开启 (Debug Mode: ON)
echo 停止服务器：请按 Ctrl+C
echo ========================================
echo.

REM 设置环境变量
set FLASK_APP=app_start.py
set FLASK_ENV=development
set FLASK_DEBUG=1

REM 运行 Flask 应用
python app_start.py

echo.
echo ========================================
echo Flask 应用已安全停止
pause