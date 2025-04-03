-- 插入默认管理员账户 (密码: 123456)
INSERT INTO users (student_id, username, email, password, role) 
VALUES ('admin', '管理员', 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBpwTTyU3VxqW', 'ADMIN')
ON DUPLICATE KEY UPDATE student_id=student_id;

-- 插入测试用户账户 (密码: 123456)
INSERT INTO users (student_id, username, email, password, role) 
VALUES ('test', '测试用户', 'test@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBpwTTyU3VxqW', 'USER')
ON DUPLICATE KEY UPDATE student_id=student_id;

-- 为测试用户添加资料
INSERT INTO user_profile (user_id, avatar, phone, address, bio)
SELECT id, 'https://via.placeholder.com/150', '13800138000', '大学城1号宿舍楼', '这是一个测试用户'
FROM users WHERE student_id = 'test'
ON DUPLICATE KEY UPDATE user_id=user_id;

-- 添加测试商品
INSERT INTO products (seller_id, title, description, price, original_price, condition_level, is_fixed_price, status, images)
SELECT id, '二手笔记本电脑', '9成新联想笔记本，i5处理器，8G内存，256G SSD', 3000.00, 6000.00, '9成新', true, 'FOR_SALE', 'https://via.placeholder.com/300'
FROM users WHERE student_id = 'test'
ON DUPLICATE KEY UPDATE seller_id=seller_id;

INSERT INTO products (seller_id, title, description, price, original_price, condition_level, is_fixed_price, status, images)
SELECT id, '考研教材', '全新考研数学、英语教材，买后未使用', 100.00, 200.00, '全新', true, 'FOR_SALE', 'https://via.placeholder.com/300'
FROM users WHERE student_id = 'test'
ON DUPLICATE KEY UPDATE seller_id=seller_id;

-- 添加测试快递单
INSERT INTO express_orders (issuer_id, pickup_location, express_number, package_size, destination, deadline, reward, contact, status)
SELECT id, '校内菜鸟驿站', 'SF1234567890', '小', '5号宿舍楼', DATE_ADD(NOW(), INTERVAL 1 DAY), 5.00, '13800138000', 'PENDING'
FROM users WHERE student_id = 'test'
ON DUPLICATE KEY UPDATE issuer_id=issuer_id;

-- 添加测试消息
INSERT INTO messages (sender_id, receiver_id, message_type, content, is_read)
SELECT NULL, id, 'SYSTEM', '欢迎使用校园互助平台！', false
FROM users WHERE student_id = 'test'
ON DUPLICATE KEY UPDATE receiver_id=receiver_id;

-- 添加测试草稿
INSERT INTO drafts (user_id, draft_type, title, content)
SELECT id, 'product', '二手自行车', '{"title":"二手自行车","description":"9成新山地自行车，骑行不到100公里","price":500,"original_price":1200,"condition_level":"9成新","is_fixed_price":true}'
FROM users WHERE student_id = 'test'
ON DUPLICATE KEY UPDATE user_id=user_id;

-- 添加测试收藏
INSERT INTO favorites (user_id, item_type, item_id)
SELECT u.id, 'product', p.id
FROM users u, products p
WHERE u.student_id = 'test' AND p.title = '考研教材'
ON DUPLICATE KEY UPDATE user_id=user_id;

-- 添加测试浏览历史
INSERT INTO browse_history (user_id, item_type, item_id)
SELECT u.id, 'product', p.id
FROM users u, products p
WHERE u.student_id = 'test' AND p.title = '二手笔记本电脑';

-- 添加测试需求
INSERT INTO wanted_items (user_id, title, description, max_budget, status)
SELECT id, '求购二手自行车', '需要一辆二手自行车代步，预算600元以内', 600.00, 'ACTIVE'
FROM users WHERE student_id = 'test'
ON DUPLICATE KEY UPDATE user_id=user_id; 