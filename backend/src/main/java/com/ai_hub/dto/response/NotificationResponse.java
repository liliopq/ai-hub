package com.ai_hub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通知列表项响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    
    /**
     * 通知ID
     */
    private Long id;
    
    /**
     * 通知类型 (COMMENT, LIKE, FOLLOW, SYSTEM)
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
     * 帖子ID（可选）
     */
    private Long postId;
    
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
