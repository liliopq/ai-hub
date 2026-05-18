-- AI Hub 社区平台数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS ai_hub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ai_hub;

-- ============================================
-- 初始管理员账号（密码：Admin@123456）
-- ============================================
-- 注意：这个密码是 BCrypt 加密后的结果
-- 如果需要修改密码，请使用 BCrypt 在线工具生成新的哈希值

INSERT INTO user (username, password, email, role, status, create_time, update_time, deleted) 
VALUES (
    'admin',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
    'admin@aihub.com',
    'ADMIN',
    1,
    NOW(),
    NOW(),
    0
) ON DUPLICATE KEY UPDATE role = 'ADMIN';

-- 提示：
-- 1. 默认管理员账号：admin
-- 2. 默认密码：Admin@123456
-- 3. 首次登录后请立即修改密码！
-- 4. 如果表中已有 admin 用户，此脚本会将其角色升级为 ADMIN
