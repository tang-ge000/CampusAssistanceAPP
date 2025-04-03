import http.server
import socketserver
import os
import sys

print("=== 开始启动服务器 ===")
print(f"当前目录: {os.getcwd()}")
print(f"Python版本: {sys.version}")

try:
    # 检查src目录
    if not os.path.exists('src'):
        print("错误: src目录不存在！")
        print(f"当前目录内容: {os.listdir('.')}")
        input("按回车键退出...")
        sys.exit(1)

    # 切换到src目录
    os.chdir('src')
    print(f"已切换到src目录: {os.getcwd()}")
    print(f"src目录内容: {os.listdir('.')}")

    # 检查login.html
    if not os.path.exists('login.html'):
        print("错误: login.html文件不存在！")
        input("按回车键退出...")
        sys.exit(1)

    # 设置端口
    PORT = 8000

    # 创建服务器
    Handler = http.server.SimpleHTTPRequestHandler
    with socketserver.TCPServer(("", PORT), Handler) as httpd:
        print(f"\n服务器已启动！")
        print(f"请使用浏览器访问: http://localhost:{PORT}/login.html")
        print("按Ctrl+C可以停止服务器")
        httpd.serve_forever()

except KeyboardInterrupt:
    print("\n服务器已停止")
except Exception as e:
    print(f"\n发生错误: {str(e)}")
finally:
    input("\n按回车键退出...") 