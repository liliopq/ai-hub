package com.ai_hub.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通知任务
 * 用于 RabbitMQ 消息队列异步处理通知
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTask {
    
    /** 通知类型：LIKE, COMMENT, FOLLOW, COLLECT, COMMENT_LIKE, SYSTEM */
    private String type;
    
    /** 接收通知的用户ID */
    private Long userId;
    
    /** 触发通知的用户ID */
    private Long sourceUserId;
    
    /** 关联的帖子ID（可选） */
    private Long postId;
    
    /** 关联的评论ID（可选） */
    private Long commentId;
    
    /** 额外内容（如评论内容，用于显示摘要） */
    private String extraContent;
    
    /** 任务创建时间 */
    private Long createTime;
}
