// 定义API基础URL
const API_BASE_URL = 'http://localhost:8080/api';

// 全局变量
let currentUser = null;

// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', () => {
    console.log('个人中心页面加载完成');
    // 检查登录状态
    checkProfileAuthStatus();
    
    // 检查是否是本地登录
    const isLocalLogin = localStorage.getItem('localLogin') === 'true';
    if (isLocalLogin) {
        console.log('检测到本地登录状态');
        showMessage('您当前使用的是本地验证登录，部分功能可能受限', 'warning', 5000);
    }
    
    // 初始化事件监听器
    initEventListeners();
    
    // 加载用户信息
    loadUserInfo();
});

// 检查登录状态 - 重命名避免与auth.js冲突
function checkProfileAuthStatus() {
    const token = localStorage.getItem('token');
    const currentPage = window.location.pathname;
    
    // 如果当前已在个人中心页面，则不重定向
    if (currentPage.includes('profile.html')) {
        console.log('已在个人中心页面，不执行重定向');
        return;
    }
    
    if (token) {
        console.log('检测到已登录状态，跳转到主页');
        window.location.href = '../pages/index.html';
    } else {
        // 未登录状态的处理
        // ...
    }
}

// 初始化事件监听器
function initEventListeners() {
    console.log('初始化事件监听器');
    // 导航菜单点击事件
    document.querySelectorAll('.sidebar a').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const targetId = link.getAttribute('href').substring(1);
            switchSection(targetId);
        });
    });

    // 退出登录按钮
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', handleLogout);
    }

    // 账号管理表单提交
    const accountForm = document.getElementById('accountForm');
    if (accountForm) {
        console.log('找到账号表单，添加提交事件监听器');
        accountForm.addEventListener('submit', handleAccountUpdate);
    } else {
        console.error('未找到账号表单');
    }
}

// 加载用户信息
function loadUserInfo() {
    const token = localStorage.getItem('token');
    console.log('尝试加载用户信息，token是否存在:', token ? '存在' : '不存在');
    
    if (!token) {
        console.error('未找到登录令牌，将重定向到登录页面');
        setTimeout(() => {
            window.location.href = '/pages/login.html';
        }, 2000);
        showMessage('未检测到登录信息，即将跳转到登录页面', 'error');
        return;
    }
    
    // 检查localStorage中是否有用户信息
    const localUser = localStorage.getItem('user');
    if (localUser) {
        try {
            const userData = JSON.parse(localUser);
            console.log('从localStorage获取到用户信息:', userData);
            
            // 直接使用本地存储的用户信息
            updateUserInfo(userData);
            showMessage('已加载本地用户信息', 'success');
            
            // 尝试从服务器获取最新信息
            fetchUserInfoFromServer(token);
            return;
        } catch (e) {
            console.error('解析本地用户信息失败:', e);
        }
    }
    
    // 显示加载状态
    showMessage('正在加载用户信息...', 'info');
    
    // 尝试解析token，查看里面是否包含用户信息
    try {
        const tokenParts = token.split('.');
        if (tokenParts.length === 3) {
            const payload = JSON.parse(atob(tokenParts[1]));
            console.log('Token中包含的信息:', payload);
            
            // 如果token中包含用户信息，可以直接使用
            if (payload.sub || payload.username || payload.userId) {
                console.log('从token中提取到用户标识:', payload.sub || payload.username || payload.userId);
            }
        }
    } catch (e) {
        console.log('token格式不是标准JWT或无法解析');
    }
    
    // 从服务器获取用户信息
    fetchUserInfoFromServer(token);
}

// 从服务器获取用户信息
function fetchUserInfoFromServer(token) {
    // 尝试不同的API路径
    // 第一次尝试: /api/profile
    let url = `${API_BASE_URL}/profile`;
    console.log('尝试请求用户信息URL:', url);
    
    // 调用API获取用户信息
    fetch(url, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => {
        console.log('服务器响应状态:', response.status, response.statusText);
        
        if (!response.ok) {
            // 如果第一个路径失败，尝试第二个
            if (response.status === 404 || response.status === 401) {
                console.log('首选路径失败，尝试备选路径...');
                return tryAlternativePathForUserInfo(token);
            }
            throw new Error(`获取用户信息失败: ${response.status} ${response.statusText}`);
        }
        return response.json();
    })
    .then(data => {
        console.log('用户信息加载成功:', data);
        
        // 检查数据结构是否符合预期
        if (!data || typeof data !== 'object') {
            console.error('获取的用户数据格式不正确:', data);
            showMessage('用户数据格式异常', 'error');
            return;
        }
        
        // 将获取的最新数据保存到localStorage
        localStorage.setItem('user', JSON.stringify(data));
        
        // 更新用户信息显示
        updateUserInfo(data);
        showMessage('用户信息已更新', 'success');
    })
    .catch(error => {
        console.error('从服务器加载用户信息失败:', error);
        
        // 如果服务器获取失败，但已经显示了localStorage中的信息，不需要再次提示错误
        const localUser = localStorage.getItem('user');
        if (!localUser) {
            showMessage('无法获取用户信息，请刷新页面重试', 'error');
        }
    });
}

// 尝试不同的API路径来获取用户信息
function tryAlternativePathForUserInfo(token) {
    // 尝试 /api/user
    let url = `${API_BASE_URL}/user`;
    console.log('尝试备选路径1:', url);
    
    return fetch(url, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => {
        console.log('备选路径1响应状态:', response.status, response.statusText);
        
        if (!response.ok) {
            // 尝试 /api/auth/user
            url = `${API_BASE_URL}/auth/user`;
            console.log('尝试备选路径2:', url);
            
            return fetch(url, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
        }
        return response;
    })
    .then(response => {
        console.log('备选路径2响应状态:', response.status, response.statusText);
        
        if (!response.ok) {
            // 尝试 /api/users/me
            url = `${API_BASE_URL}/users/me`;
            console.log('尝试备选路径3:', url);
            
            return fetch(url, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
        }
        return response;
    })
    .then(response => {
        if (!response.ok) {
            console.error('所有API路径尝试获取用户信息均失败');
            throw new Error('无法获取用户信息');
        }
        return response.json();
    });
}

// 获取测试用户数据
function getTestUserData() {
    return {
        username: '测试用户',
        studentId: '2023001001',
        email: 'test@example.com',
        avatar: 'assets/img/default-avatar.png'
    };
}

// 更新用户信息显示
function updateUserInfo(data) {
    console.log('正在更新界面上的用户信息:', data);
    
    // 确保data是对象类型
    if (!data || typeof data !== 'object') {
        console.error('无法更新用户信息：数据无效');
        return;
    }
    
    // 学号可能存储在不同字段中
    const studentId = data.studentId || data.student_id || '';
    
    // 更新用户名和基本信息
    const usernameElement = document.getElementById('username');
    if (usernameElement) usernameElement.textContent = data.username || '用户名';
    
    const studentIdElement = document.getElementById('student-id');
    if (studentIdElement) studentIdElement.textContent = `学号：${studentId}`;
    
    const emailElement = document.getElementById('email');
    if (emailElement) emailElement.textContent = `邮箱：${data.email || ''}`;
    
    // 更新表单中的值
    const updateUsernameInput = document.getElementById('update-username');
    if (updateUsernameInput) updateUsernameInput.value = data.username || '';
    
    const updateEmailInput = document.getElementById('update-email');
    if (updateEmailInput) updateEmailInput.value = data.email || '';
    
    // 更新头像 - 支持不同的图像URL格式
    const avatarUrl = data.avatar || data.avatarUrl || '';
    const avatarElement = document.querySelector('.avatar');
    if (avatarElement) {
        if (avatarUrl) {
            avatarElement.style.backgroundImage = `url(${avatarUrl})`;
            avatarElement.innerHTML = '';
        } else {
            // 如果没有头像，显示用户名首字母
            const initial = (data.username || '用户')[0].toUpperCase();
            avatarElement.style.backgroundImage = 'none';
            avatarElement.innerHTML = `<span>${initial}</span>`;
        }
    }
    
    // 在控制台输出确认信息
    console.log(`用户信息已更新。用户名: ${data.username}, 学号: ${studentId}`);
}

// 切换内容区域
function switchSection(sectionId) {
    console.log('切换到区域:', sectionId);
    
    // 更新导航菜单激活状态
    document.querySelectorAll('.sidebar a').forEach(link => {
        link.classList.remove('active');
        if (link.getAttribute('href') === `#${sectionId}`) {
            link.classList.add('active');
        }
    });

    // 更新内容区域显示
    document.querySelectorAll('.content-section').forEach(section => {
        section.classList.remove('active');
    });
    
    const targetSection = document.getElementById(`${sectionId}-section`);
    if (targetSection) {
        targetSection.classList.add('active');
        // 加载对应区域的数据
        loadSectionData(sectionId);
    }
}

// 加载区域数据
function loadSectionData(sectionId) {
    console.log('加载区域数据:', sectionId);
    
    const token = localStorage.getItem('token');
    
    switch (sectionId) {
        case 'account':
            // 账号区域不需要额外加载数据
            break;
        case 'messages':
            loadMessages(token);
            break;
        case 'history':
            loadHistory(token);
            break;
        case 'drafts':
            loadDrafts(token);
            break;
        case 'favorites':
            loadFavorites(token);
            break;
        case 'purchased':
            loadPurchased(token);
            break;
        case 'wanted':
            loadWanted(token);
            break;
        case 'published':
            loadPublished(token);
            break;
        case 'sold':
            loadSold(token);
            break;
    }
}

// 加载消息列表
function loadMessages(token) {
    fetch(`${API_BASE_URL}/profile/messages`, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => response.json())
    .then(messages => {
        const messageList = document.querySelector('.message-list');
        messageList.innerHTML = messages.map(message => `
            <div class="message-item">
                <h3>${message.title}</h3>
                <p>${message.content}</p>
                <span class="message-time">${new Date(message.createdAt).toLocaleString()}</span>
            </div>
        `).join('');
    })
    .catch(error => {
        console.error('加载消息失败:', error);
        showMessage('加载消息失败，请刷新页面重试', 'error');
    });
}

// 加载历史记录
function loadHistory(token) {
    fetch(`${API_BASE_URL}/profile/history`, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => response.json())
    .then(history => {
        const historyList = document.querySelector('.history-list');
        historyList.innerHTML = history.map(item => `
            <div class="history-item">
                <h3>${item.title}</h3>
                <p>${item.description}</p>
                <span class="history-time">${new Date(item.visitedAt).toLocaleString()}</span>
            </div>
        `).join('');
    })
    .catch(error => {
        console.error('加载历史记录失败:', error);
        showMessage('加载历史记录失败，请刷新页面重试', 'error');
    });
}

// 加载草稿箱
function loadDrafts(token) {
    fetch(`${API_BASE_URL}/profile/drafts`, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => response.json())
    .then(drafts => {
        const draftList = document.querySelector('.draft-list');
        draftList.innerHTML = drafts.map(draft => `
            <div class="draft-item">
                <h3>${draft.title}</h3>
                <p>${draft.content}</p>
                <div class="draft-actions">
                    <button class="btn-primary" onclick="editDraft(${draft.id})">编辑</button>
                    <button class="btn-secondary" onclick="deleteDraft(${draft.id})">删除</button>
                </div>
            </div>
        `).join('');
    })
    .catch(error => {
        console.error('加载草稿失败:', error);
        showMessage('加载草稿失败，请刷新页面重试', 'error');
    });
}

// 加载收藏列表
function loadFavorites(token) {
    fetch(`${API_BASE_URL}/profile/favorites`, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => response.json())
    .then(favorites => {
        const favoriteList = document.querySelector('.favorite-list');
        favoriteList.innerHTML = favorites.map(favorite => `
            <div class="favorite-item">
                <h3>${favorite.title}</h3>
                <p>${favorite.description}</p>
                <div class="favorite-actions">
                    <button class="btn-primary" onclick="viewFavorite(${favorite.id})">查看</button>
                    <button class="btn-secondary" onclick="removeFavorite(${favorite.id})">取消收藏</button>
                </div>
            </div>
        `).join('');
    })
    .catch(error => {
        console.error('加载收藏失败:', error);
        showMessage('加载收藏失败，请刷新页面重试', 'error');
    });
}

// 加载购买记录
function loadPurchased(token) {
    fetch(`${API_BASE_URL}/purchased`, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => response.json())
    .then(purchases => {
        const purchaseList = document.querySelector('.purchase-list');
        purchaseList.innerHTML = purchases.map(purchase => `
            <div class="purchase-item">
                <h3>${purchase.title}</h3>
                <p>价格：${purchase.price}</p>
                <p>购买时间：${new Date(purchase.purchasedAt).toLocaleString()}</p>
                <div class="purchase-actions">
                    <button class="btn-primary" onclick="viewPurchase(${purchase.id})">查看详情</button>
                </div>
            </div>
        `).join('');
    })
    .catch(error => {
        console.error('加载购买记录失败:', error);
        showMessage('加载购买记录失败，请刷新页面重试', 'error');
    });
}

// 加载想要列表
function loadWanted(token) {
    fetch(`${API_BASE_URL}/wanted`, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => response.json())
    .then(wantedItems => {
        const wantedList = document.querySelector('.wanted-list');
        wantedList.innerHTML = wantedItems.map(item => `
            <div class="wanted-item">
                <h3>${item.title}</h3>
                <p>${item.description}</p>
                <div class="wanted-actions">
                    <button class="btn-primary" onclick="viewWanted(${item.id})">查看</button>
                    <button class="btn-secondary" onclick="removeWanted(${item.id})">取消</button>
                </div>
            </div>
        `).join('');
    })
    .catch(error => {
        console.error('加载想要列表失败:', error);
        showMessage('加载想要列表失败，请刷新页面重试', 'error');
    });
}

// 加载发布列表
function loadPublished(token) {
    fetch(`${API_BASE_URL}/published`, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => response.json())
    .then(publishedItems => {
        const publishedList = document.querySelector('.published-list');
        publishedList.innerHTML = publishedItems.map(item => `
            <div class="published-item">
                <h3>${item.title}</h3>
                <p>${item.description}</p>
                <p>发布时间：${new Date(item.publishedAt).toLocaleString()}</p>
                <div class="published-actions">
                    <button class="btn-primary" onclick="editPublished(${item.id})">编辑</button>
                    <button class="btn-secondary" onclick="deletePublished(${item.id})">删除</button>
                </div>
            </div>
        `).join('');
    })
    .catch(error => {
        console.error('加载发布列表失败:', error);
        showMessage('加载发布列表失败，请刷新页面重试', 'error');
    });
}

// 加载卖出记录
function loadSold(token) {
    fetch(`${API_BASE_URL}/sold`, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => response.json())
    .then(soldItems => {
        const soldList = document.querySelector('.sold-list');
        soldList.innerHTML = soldItems.map(item => `
            <div class="sold-item">
                <h3>${item.title}</h3>
                <p>价格：${item.price}</p>
                <p>卖出时间：${new Date(item.soldAt).toLocaleString()}</p>
                <div class="sold-actions">
                    <button class="btn-primary" onclick="viewSold(${item.id})">查看详情</button>
                </div>
            </div>
        `).join('');
    })
    .catch(error => {
        console.error('加载卖出记录失败:', error);
        showMessage('加载卖出记录失败，请刷新页面重试', 'error');
    });
}

// 处理退出登录
function handleLogout() {
    console.log('退出登录');
    localStorage.removeItem('token');
    // 使用绝对路径跳转到登录页面
    window.location.href = '/pages/login.html';
}

// 添加刷新token功能
async function refreshAuthToken() {
    const currentToken = localStorage.getItem('token');
    if (!currentToken) {
        console.error('没有找到token，无法刷新');
        return null;
    }
    
    try {
        console.log('尝试刷新token...');
        const response = await fetch(`${API_BASE_URL}/auth/refresh`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${currentToken}`
            }
        });
        
        if (response.ok) {
            const data = await response.json();
            if (data.token) {
                console.log('token刷新成功，保存新token');
                localStorage.setItem('token', data.token);
                return data.token;
            }
        }
        
        // 如果刷新失败，尝试直接重新登录
        return await autoRelogin();
    } catch (error) {
        console.error('刷新token失败:', error);
        return currentToken; // 返回原token
    }
}

// 自动重新登录
async function autoRelogin() {
    try {
        // 从localStorage获取登录信息
        const savedUser = localStorage.getItem('user');
        if (!savedUser) return null;
        
        const user = JSON.parse(savedUser);
        if (!user.studentId) return null;
        
        console.log('尝试自动重新登录...');
        
        // 模拟自动登录请求
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                studentId: user.studentId,
                // 注意：这里不能发送密码，因为我们不保存密码
                // 这只是一个示例，实际中需要后端支持其他登录方式
                autoLogin: true
            })
        });
        
        if (response.ok) {
            const data = await response.json();
            if (data.token) {
                localStorage.setItem('token', data.token);
                return data.token;
            }
        }
        
        return null;
    } catch (error) {
        console.error('自动重新登录失败:', error);
        return null;
    }
}

// 添加模拟数据库更新成功处理函数
function showDbUpdateSuccess() {
    const successNotification = document.querySelector('.db-update-success');
    if (successNotification) {
        successNotification.style.display = 'block';
        
        // 添加关闭按钮事件
        const closeBtn = successNotification.querySelector('.close-btn');
        if (closeBtn) {
            closeBtn.onclick = function() {
                successNotification.style.display = 'none';
            };
        }
        
        // 5秒后自动关闭
        setTimeout(function() {
            successNotification.style.display = 'none';
        }, 5000);
    }
}

// 处理密码修改后的重定向
function redirectToLogin(message = '密码已修改，请重新登录') {
    // 显示提示消息
    showMessage(message, 'success');
    
    // 清除所有会话数据
    setTimeout(() => {
        // 清除登录令牌
        localStorage.removeItem('token');
        
        // 保留用户基本信息，但标记为未登录状态
        const userStr = localStorage.getItem('user');
        if (userStr) {
            try {
                const user = JSON.parse(userStr);
                user.passwordChanged = true; // 标记密码已修改
                user.lastLogout = new Date().toISOString(); // 记录登出时间
                localStorage.setItem('user', JSON.stringify(user));
            } catch (e) {
                console.error('处理用户数据时出错:', e);
            }
        }
        
        // 使用绝对路径跳转到登录页面，解决相对路径导致的404问题
        window.location.href = '/pages/login.html?passwordChanged=true';
    }, 1500);
}

// 处理账号更新
function handleAccountUpdate(e) {
    e.preventDefault();
    console.log('提交账号更新');
    
    const formData = new FormData(document.getElementById('accountForm'));
    
    // 准备数据，只包含非空值
    const data = {};
    if (formData.get('username')) data.username = formData.get('username');
    if (formData.get('email')) data.email = formData.get('email');
    
    const currentPassword = formData.get('currentPassword');
    const newPassword = formData.get('password');
    const confirmPassword = formData.get('confirmPassword');
    
    console.log('表单数据:', data);
    
    // 检查是否在修改密码
    if (newPassword) {
        // 如果要修改密码，则必须提供当前密码
        if (!currentPassword) {
            showMessage('请输入当前密码以验证身份', 'error');
            return;
        }
        
        // 检查密码确认
        if (newPassword !== confirmPassword) {
            showMessage('两次输入的新密码不一致', 'error');
            return;
        }
        
        // 将密码信息添加到数据中
        data.currentPassword = currentPassword;
        data.password = newPassword;
    }
    
    // 检查是否有数据要更新
    if (Object.keys(data).length === 0) {
        showMessage('请填写至少一项要修改的内容', 'warning');
        return;
    }
    
    // 显示等待消息
    showMessage('正在更新个人资料...', 'info');
    
    // 调用更新服务
    updateUserData(data)
        .then(result => {
            console.log('更新结果:', result);
            
            if (result.success) {
                // 显示更新成功消息
                showMessage(result.message || '个人资料更新成功！', 'success');
                
                // 显示数据库更新成功提示
                if (result.dbUpdated) {
                    showDbUpdateSuccess();
                }
                
                // 判断是否修改了密码
                if (result.passwordChanged) {
                    // 如果修改了密码，跳转到登录页面
                    setTimeout(() => {
                        redirectToLogin(result.message || '密码已修改，请重新登录');
                    }, 1000);
                    return;
                }
                
                // 刷新用户界面
                if (result.userData) {
                    updateUserInfo(result.userData);
                }
            } else {
                showMessage(result.message || '更新失败，请重试', 'error');
            }
        })
        .catch(error => {
            console.error('更新过程出错:', error);
            showMessage('更新失败:' + (error.message || '未知错误'), 'error');
        });
}

// 统一的用户数据更新服务
async function updateUserData(data) {
    try {
        // 处理密码更新的特殊情况
        if (data.password && data.currentPassword) {
            // 先验证当前密码
            const passwordVerified = await verifyCurrentPassword(data.currentPassword);
            if (!passwordVerified) {
                return { 
                    success: false, 
                    message: '当前密码验证失败，无法更新密码'
                };
            }
            console.log('当前密码验证成功，继续更新密码');
            
            // 使用专门的密码修改API
            try {
                // 获取用户信息
                const userStr = localStorage.getItem('user');
                if (!userStr) {
                    return { 
                        success: false, 
                        message: '无法获取用户信息'
                    };
                }
                
                const user = JSON.parse(userStr);
                const studentId = user.studentId;
                
                if (!studentId) {
                    return { 
                        success: false, 
                        message: '用户学号信息缺失'
                    };
                }
                
                console.log('调用密码修改API');
                const response = await fetch(`${API_BASE_URL}/auth/changePassword`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${localStorage.getItem('token')}`
                    },
                    body: JSON.stringify({
                        studentId: studentId,
                        currentPassword: data.currentPassword,
                        newPassword: data.password
                    })
                });
                
                console.log('密码修改API响应状态:', response.status);
                const responseData = await response.json();
                console.log('密码修改API响应:', responseData);
                
                if (response.ok) {
                    return { 
                        success: true, 
                        message: responseData.message || '密码修改成功，请重新登录', 
                        passwordChanged: true 
                    };
                } else {
                    return { 
                        success: false, 
                        message: responseData.message || responseData.error || '密码修改失败'
                    };
                }
            } catch (err) {
                console.error('密码修改API调用失败:', err);
                return { 
                    success: false, 
                    message: '无法连接到服务器，密码修改失败'
                };
            }
        }
        
        // 非密码更新的普通用户信息更新
        const token = localStorage.getItem('token');
        
        // 尝试调用后端API
        let serverUpdateSuccess = false;
        let serverResult = null;
        
        try {
            // 准备要更新的数据
            const updateData = {};
            if (data.username) updateData.username = data.username;
            if (data.email) updateData.email = data.email;
            
            // 获取用户ID
            const userStr = localStorage.getItem('user');
            if (!userStr) {
                return { 
                    success: false, 
                    message: '无法获取用户信息',
                };
            }
            
            const user = JSON.parse(userStr);
            const userId = user.id;
            
            // 尝试调用API更新用户信息
            const response = await fetch(`${API_BASE_URL}/users/${userId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(updateData)
            });
            
            if (response.ok) {
                try {
                    serverResult = await response.json();
                    serverUpdateSuccess = true;
                    console.log('服务器更新成功:', serverResult);
                } catch (e) {
                    console.warn('解析服务器响应失败:', e);
                }
            }
        } catch (apiError) {
            console.warn('API调用失败:', apiError);
            // 继续执行，不中断流程
        }
        
        // 无论后端是否成功，都更新本地数据
        const userStr = localStorage.getItem('user');
        if (!userStr) {
            return { 
                success: serverUpdateSuccess, 
                message: '无法获取用户信息',
                dbUpdated: serverUpdateSuccess,
                userData: serverResult
            };
        }
        
        const user = JSON.parse(userStr);
        
        // 更新本地用户数据
        if (data.username) user.username = data.username;
        if (data.email) user.email = data.email;
        
        // 保存到localStorage
        localStorage.setItem('user', JSON.stringify(user));
        
        return { 
            success: true, 
            message: serverUpdateSuccess ? '数据已成功更新到数据库' : '数据已在本地更新',
            dbUpdated: serverUpdateSuccess,
            userData: user
        };
    } catch (error) {
        console.error('数据更新过程出错:', error);
        return { success: false, message: error.message || '更新过程出错' };
    }
}

// 验证当前密码
async function verifyCurrentPassword(currentPassword) {
    try {
        const token = localStorage.getItem('token');
        if (!token) {
            showMessage('您的登录凭证已失效，请重新登录', 'error');
            return false;
        }
        
        const userStr = localStorage.getItem('user');
        if (!userStr) {
            showMessage('无法获取用户信息', 'error');
            return false;
        }
        
        const user = JSON.parse(userStr);
        const studentId = user.studentId;
        
        if (!studentId) {
            showMessage('用户信息不完整', 'error');
            return false;
        }
        
        console.log('尝试验证当前密码');
        
        // 调用验证密码的API
        const response = await fetch(`${API_BASE_URL}/auth/verify-password`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({
                studentId: studentId,
                password: currentPassword
            })
        });
        
        console.log('密码验证API响应状态:', response.status);
        
        if (!response.ok) {
            console.log('密码验证API返回错误:', response.status);
            return false;
        }
        
        const result = await response.json();
        console.log('密码验证结果:', result);
        return result.verified === true;
    } catch (error) {
        console.error('验证密码出错:', error);
        showMessage('验证密码过程中出错', 'error');
        return false;
    }
}

// 显示消息
function showMessage(message, type = 'info', duration = 3000) {
    console.log('显示消息:', message, type);
    const messageElement = document.getElementById('message');
    if (messageElement) {
        messageElement.textContent = message;
        messageElement.className = `message ${type}`;
        messageElement.style.display = 'block';
        
        setTimeout(() => {
            messageElement.style.display = 'none';
        }, duration);
    } else {
        console.error('未找到消息元素');
        alert(message);
    }
} 