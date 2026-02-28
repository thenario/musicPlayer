@echo off
setlocal enabledelayedexpansion
chcp 936 > nul
title Flask应用停止脚本

echo ================================
echo       Flask应用停止脚本
echo ================================

echo.
echo [1] 区分不同类型的进程：
echo   类型         进程名            说明
echo   ---------   -------------     --------------------
echo   Flask应用   python.exe        您的Flask服务器
echo   浏览器       msedge.exe       访问Flask的浏览器
echo.

echo [2] 停止Flask相关进程...
echo.

REM 查找并停止Flask应用（端口5000）
set FLASK_STOPPED=0
setlocal disabledelayedexpansion
for /f "tokens=5" %%a in ('netstat -ano 2^>nul ^| findstr ":5000 "') do (
    echo 找到占用端口5000的进程ID: %%a
    tasklist /FI "PID eq %%a" 2>nul | findstr "python.exe" >nul
    
    REM 检查上一条命令的返回码
    if not errorlevel 1 (
        echo → 这是Flask应用进程，正在停止...
        taskkill /F /PID %%a 2>nul >nul
        set FLASK_STOPPED=1
    ) else (
        tasklist /FI "PID eq %%a" 2>nul | findstr "msedge.exe" >nul
        if not errorlevel 1 (
            echo → 这是浏览器进程，不需要停止
        ) else (
            echo → 这是其他进程（PID: %%a），跳过...
        )
    )
)

if "%FLASK_STOPPED%"=="0" (
    echo 未找到运行在端口5000的Flask进程
)

echo.
echo [3] 快速停止方案选择：
echo.
echo [1] 只停止端口5000的进程（推荐）
echo [2] 停止所有Python进程
echo [3] 查看当前进程但不停止
echo.
set /p choice="请选择 (1/2/3, 默认1): "

if "%choice%"=="2" (
    echo 正在停止所有Python进程...
    taskkill /F /IM python.exe 2>nul
    if errorlevel 0 (
        echo ? 已停止python.exe进程
    ) else (
        echo 没有python.exe进程在运行
    )
    
    taskkill /F /IM pythonw.exe 2>nul
    if errorlevel 0 (
        echo ? 已停止pythonw.exe进程
    ) else (
        echo 没有pythonw.exe进程在运行
    )
) else if "%choice%"=="3" (
    echo.
    echo 当前Python进程：
    tasklist | findstr /i "python"
    echo.
    echo 当前端口5000状态：
    netstat -ano | findstr ":5000 "
    echo.
    set /p pause_temp="按回车键继续..."
) else (
    echo 已执行端口5000进程清理
)

echo.
echo [4] 操作完成！
echo.
echo 提示：如果之前运行了Flask应用：
echo 1. 现在可以重新启动：运行 start.bat
echo 2. 或者按任意键关闭此窗口
echo ================================
pause
exit /b 0