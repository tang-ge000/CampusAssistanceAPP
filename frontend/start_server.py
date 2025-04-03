import os
import sys
import http.server
import socketserver
import webbrowser

print("=== 开始启动服务器 ===")
print(f"当前目录: {os.getcwd()}")
print(f"Python版本: {sys.version}")

# 确保工作在frontend目录
script_dir = os.path.dirname(os.path.abspath(__file__))
os.chdir(script_dir)
print(f"设置工作目录: {os.getcwd()}")

# 检查pages目录
if not os.path.exists('pages'):
    print("错误：找不到pages目录")
    print(f"当前目录内容: {os.listdir('.')}")
    input("按回车键退出...")
    sys.exit(1)

# 检查src/assets目录
if not os.path.exists('src/assets'):
    print("错误：找不到src/assets目录")
    print(f"src目录内容: {os.listdir('src')}")
    input("按回车键退出...")
    sys.exit(1)

# 创建表示HTML文件中资源路径的符号链接
os.makedirs('pages/src', exist_ok=True)
if not os.path.exists('pages/src/assets') and os.path.exists('src/assets'):
    try:
        if os.name == 'nt':  # Windows
            import subprocess
            subprocess.run(['mklink', '/J', 'pages\\src\\assets', '..\\src\\assets'], 
                          shell=True, check=True)
            print("已创建符号链接: pages/src/assets -> ../src/assets")
        else:  # Linux/Mac
            os.symlink('../../src/assets', 'pages/src/assets')
            print("已创建符号链接: pages/src/assets -> ../../src/assets")
    except Exception as e:
        print(f"创建符号链接失败: {str(e)}")
        print("将使用重定向处理资源路径")

# 自定义HTTP请求处理器
class EncodingFixedHTTPRequestHandler(http.server.SimpleHTTPRequestHandler):
    def do_GET(self):
        print(f"收到请求: {self.path}")
        
        # 如果访问根目录或无扩展名的路径，重定向到相应页面
        if self.path == '/':
            self.send_response(302)
            self.send_header('Location', '/pages/index.html')
            self.end_headers()
            return
        
        # 如果直接访问HTML文件，重定向到pages目录
        elif self.path.endswith('.html') and not self.path.startswith('/pages/'):
            self.send_response(302)
            self.send_header('Location', f'/pages{self.path}')
            self.end_headers()
            return
        
        # 处理无扩展名的路径
        elif self.path in ['/login', '/index', '/profile', '/market', '/express', 
                          '/product-detail', '/my-products', '/publish-product', '/wanted-product']:
            self.send_response(302)
            self.send_header('Location', f'/pages{self.path}.html')
            self.end_headers()
            return
            
        return http.server.SimpleHTTPRequestHandler.do_GET(self)
    
    def send_header(self, keyword, value):
        """重写send_header方法，确保HTML文件使用UTF-8编码"""
        if keyword.lower() == 'content-type' and value.startswith('text/html'):
            value = 'text/html; charset=utf-8'
        super().send_header(keyword, value)
    
    def end_headers(self):
        """添加缓存控制和CORS头"""
        self.send_header("Cache-Control", "no-cache, no-store, must-revalidate")
        self.send_header("Pragma", "no-cache")
        self.send_header("Expires", "0")
        self.send_header("Access-Control-Allow-Origin", "*")
        super().end_headers()

# 设置端口
PORT = 8000

# 创建HTTP服务器
Handler = EncodingFixedHTTPRequestHandler
httpd = socketserver.TCPServer(("", PORT), Handler)

print(f"服务器启动在 http://localhost:{PORT}")
print("按 Ctrl+C 停止服务器")

# 显示目录内容
print(f"当前目录内容: {os.listdir('.')}")
print(f"pages目录内容: {os.listdir('pages')}")
print(f"src/assets目录内容: {os.listdir('src/assets')}")

# 自动打开浏览器
webbrowser.open(f'http://localhost:{PORT}/pages/index.html')

try:
    httpd.serve_forever()
except KeyboardInterrupt:
    print("\n服务器已停止")
except Exception as e:
    print(f"\n发生错误: {str(e)}")
finally:
    input("\n按回车键退出...") 