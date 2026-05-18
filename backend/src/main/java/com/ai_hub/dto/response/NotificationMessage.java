package com.ai_hub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * WebSocket通知消息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {

    /**
     * 通知ID
     */
    private Long id;

    /**
     * 通知类型：LIKE, COMMENT, FOLLOW, SYSTEM
     */
    private String type;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 来源用户信息
     */
    private UserBasicInfo sourceUser;

    /**
     * 帖子ID（点赞、评论通知）
     */
    private Long postId;

    /**
     * 评论ID（评论通知）
     */
    private Long commentId;

    /**
     * 是否已读
     */
    private Boolean isRead;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 用户基本信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserBasicInfo {
        private Long id;
        private String username;
        private String avatar;
    }
}
