package com.ai_hub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 评论响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    
    /**
     * 评论ID
     */
    private Long id;
    
    /**
     * 评论内容
     */
    private String content;
    
    /**
     * 评论用户信息
     */
    private UserBasicInfo user;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 用户基本信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserBasicInfo {
        private Long id;
        private String username;
        private String avatar;
    }
}
