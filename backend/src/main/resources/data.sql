-- 插入默认管理员账户 (密码: 123456)
INSERT INTO users (student_id, username, email, password, role) 
VALUES ('admin', '管理员', 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBpwTTyU3VxqW', 'ADMIN')
ON DUPLICATE KEY UPDATE student_id=student_id;

-- 插入测试用户账户 (密码: 123456)
INSERT INTO users (student_id, username, email, password, role) 
VALUES ('test', '测试用户', 'test@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBpwTTyU3VxqW', 'USER')
ON DUPLICATE KEY UPDATE student_id=student_id; 