@echo off
:: 设置代码页为UTF-8
chcp 65001 >nul
:: 设置控制台字体为支持中文的字体
REG ADD "HKEY_CURRENT_USER\Console\%CD:~0,2%" /v "FaceName" /t REG_SZ /d "新宋体" /f >nul

echo 正在启动校园互助平台...

:: 检查并关闭已存在的Java和Python进程
echo 正在检查并关闭已存在的服务...
for /f "tokens=5" %%a in ('netstat -aon ^| find ":8080" ^| find "LISTENING"') do (
    if not "%%a"=="0" (
        taskkill /F /PID %%a 2>nul
        echo 已关闭端口8080的进程
    )
)
for /f "tokens=5" %%a in ('netstat -aon ^| find ":8000" ^| find "LISTENING"') do (
    if not "%%a"=="0" (
        taskkill /F /PID %%a 2>nul
        echo 已关闭端口8000的进程
    )
)
for /f "tokens=5" %%a in ('netstat -aon ^| find ":8081" ^| find "LISTENING"') do (
    if not "%%a"=="0" (
        taskkill /F /PID %%a 2>nul
        echo 已关闭端口8081的进程
    )
)

:: 等待进程完全关闭
timeout /t 2 /nobreak

:: 启动后端服务
echo 正在启动后端服务...
start cmd /k "cd backend && mvn clean && mvn spring-boot:run"

:: 等待5秒让后端启动
timeout /t 5 /nobreak

:: 启动前端服务
echo 正在启动前端服务...
start cmd /k "cd frontend && D:\python-edition\python-3.13\python.exe start_server.py"

echo 服务启动完成！
echo 请使用浏览器访问: http://localhost:8000/login.html
echo.
echo 测试账号:
echo 管理员账号 - 学号: admin, 密码: 123456
echo 测试用户账号 - 学号: test, 密码: 123456
echo.
echo 按任意键退出...
pause > nul

:: 关闭所有服务
echo 正在关闭所有服务...
for /f "tokens=5" %%a in ('netstat -aon ^| find ":8080" ^| find "LISTENING"') do (
    if not "%%a"=="0" (
        taskkill /F /PID %%a 2>nul
        echo 已关闭端口8080的进程
    )
)
for /f "tokens=5" %%a in ('netstat -aon ^| find ":8000" ^| find "LISTENING"') do (
    if not "%%a"=="0" (
        taskkill /F /PID %%a 2>nul
        echo 已关闭端口8000的进程
    )
)
for /f "tokens=5" %%a in ('netstat -aon ^| find ":8081" ^| find "LISTENING"') do (
    if not "%%a"=="0" (
        taskkill /F /PID %%a 2>nul
        echo 已关闭端口8081的进程
    )
)

:: 强制结束所有Java和Python进程
taskkill /F /IM java.exe 2>nul
taskkill /F /IM python.exe 2>nul

echo 所有服务已关闭！
timeout /t 2 