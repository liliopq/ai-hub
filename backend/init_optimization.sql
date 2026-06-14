-- ============================================
-- AI Hub 数据库优化迁移脚本
-- 1. 创建 ai_message 表（AI 对话上下文）
-- 2. 为 post 表添加全文索引（帖子搜索优化）
-- ============================================

-- 1. 创建 AI 消息表
CREATE TABLE IF NOT EXISTS `ai_message` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `session_id` VARCHAR(64) NOT NULL COMMENT '会话ID',
    `role` VARCHAR(20) NOT NULL COMMENT '消息角色：USER / ASSISTANT',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `seq` INT NOT NULL DEFAULT 0 COMMENT '消息序号（用于排序）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_session_id` (`session_id`),
    INDEX `idx_session_seq` (`session_id`, `seq`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 对话消息表';

-- 2. 为 post 表添加全文索引（MySQL InnoDB 原生全文检索）
-- 用于优化帖子标题和内容的搜索性能，替代 LIKE '%keyword%' 全表扫描
ALTER TABLE `post` ADD FULLTEXT INDEX `ft_post_search` (`title`, `content`);
