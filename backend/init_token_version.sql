-- ============================================
-- Token 版本号机制 - 数据库迁移脚本
-- ============================================
-- 功能说明：
--   1. 在用户表中增加 token_version 字段，默认 0
--   2. 登录生成 Token 时，从数据库读取 token_version 并存入 Token
--   3. 修改密码时，将 token_version +1，使所有旧 Token 失效
--   4. 验证 Token 时，比对 Token 中的版本号与数据库中的版本号

USE ai_hub;

-- 添加 token_version 字段（如果不存在）
-- 使用存储过程来安全地添加字段，避免重复执行时报错
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS add_token_version_column()
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = 'ai_hub'
        AND TABLE_NAME = 'user'
        AND COLUMN_NAME = 'token_version'
    ) THEN
        ALTER TABLE `user`
        ADD COLUMN `token_version` INT NOT NULL DEFAULT 0
        COMMENT 'Token版本号，修改密码时自动+1，用于使旧Token失效';
    END IF;
END //
DELIMITER ;

-- 执行存储过程
CALL add_token_version_column();

-- 清理存储过程
DROP PROCEDURE IF EXISTS add_token_version_column;

-- 更新现有用户的 token_version 为 0（如果之前没有此字段）
UPDATE `user` SET `token_version` = 0 WHERE `token_version` IS NULL;

-- 验证结果
SELECT 'Token 版本号字段迁移完成' AS message;
SELECT COUNT(*) AS total_users,
       SUM(CASE WHEN token_version = 0 THEN 1 ELSE 0 END) AS version_0_users,
       SUM(CASE WHEN token_version > 0 THEN 1 ELSE 0 END) AS version_gt_0_users
FROM `user`;
