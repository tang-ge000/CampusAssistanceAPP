// 全局变量
const API_BASE_URL = 'http://localhost:8080/api';

// DOM 元素
const loginForm = document.getElementById('loginForm');
const registerForm = document.getElementById('registerForm');

// 检查登录状态
function checkAuthStatus() {
    const token = localStorage.getItem('token');
    if (token) {
        console.log('检测到已登录状态，跳转到主页');
        window.location.href = 'index.html';
    }
}

// 切换登录/注册表单
function switchTab(tab) {
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');
    const tabs = document.querySelectorAll('.tab-btn');

    if (tab === 'login') {
        loginForm.style.display = 'block';
        registerForm.style.display = 'none';
        tabs[0].classList.add('active');
        tabs[1].classList.remove('active');
    } else {
        loginForm.style.display = 'none';
        registerForm.style.display = 'block';
        tabs[0].classList.remove('active');
        tabs[1].classList.add('active');
    }
}

// 处理登录
async function handleLogin(event) {
    event.preventDefault();
    
    const studentId = document.getElementById('loginStudentId').value;
    const password = document.getElementById('loginPassword').value;

    console.log('尝试登录:', { studentId, password });

    try {
        const requestBody = {
            studentId: studentId,
            password: password
        };
        console.log('发送登录请求到:', `${API_BASE_URL}/auth/login`);
        console.log('请求体:', JSON.stringify(requestBody, null, 2));

        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(requestBody)
        });

        console.log('登录响应状态:', response.status);
        const responseText = await response.text();
        console.log('登录响应内容:', responseText);

        if (response.ok) {
            const data = JSON.parse(responseText);
            console.log('登录成功，保存token和用户信息');
            localStorage.setItem('token', data.token);
            localStorage.setItem('user', JSON.stringify(data.user));
            showMessage('登录成功！', 'success');
            console.log('准备跳转到主页');
            setTimeout(() => {
                window.location.href = 'index.html';
            }, 1000);
        } else {
            let errorMessage = '登录失败，请检查学号和密码';
            try {
                const errorData = JSON.parse(responseText);
                errorMessage = errorData.message || errorData.error || errorMessage;
                console.log('登录失败:', errorMessage);
            } catch (e) {
                console.error('解析错误响应失败:', e);
            }
            showMessage(errorMessage, 'error');
        }
    } catch (error) {
        console.error('登录请求失败:', error);
        showMessage('登录失败，请稍后重试', 'error');
    }
}

// 处理注册
async function handleRegister(event) {
    event.preventDefault();
    
    const studentId = document.getElementById('registerStudentId').value;
    const username = document.getElementById('registerUsername').value;
    const email = document.getElementById('registerEmail').value;
    const password = document.getElementById('registerPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    // 表单验证
    if (password !== confirmPassword) {
        showMessage('两次输入的密码不一致', 'error');
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({
                studentId,
                username,
                email,
                password
            })
        });

        const data = await response.json();

        if (response.ok) {
            showMessage('注册成功！请登录', 'success');
            switchTab('login');
        } else {
            showMessage(data.error || '注册失败，请稍后重试', 'error');
        }
    } catch (error) {
        console.error('注册请求失败:', error);
        showMessage('注册失败，请稍后重试', 'error');
    }
}

// 显示消息
function showMessage(message, type) {
    const messageDiv = document.getElementById('message');
    messageDiv.textContent = message;
    messageDiv.className = `message ${type}`;
    messageDiv.style.display = 'block';
    setTimeout(() => {
        messageDiv.style.display = 'none';
    }, 3000);
}

// 页面加载时检查登录状态
document.addEventListener('DOMContentLoaded', () => {
    console.log('页面加载完成，检查登录状态');
    checkAuthStatus();
    
    // 添加表单提交事件监听器
    if (loginForm) {
        console.log('添加登录表单提交事件监听器');
        loginForm.addEventListener('submit', handleLogin);
    }
    
    if (registerForm) {
        console.log('添加注册表单提交事件监听器');
        registerForm.addEventListener('submit', handleRegister);
    }
}); 