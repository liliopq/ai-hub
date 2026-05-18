package com.ai_hub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子详情响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDetailResponse {
    private Long id;
    private String title;
    private String content;
    private UserBasicInfo user;
    private String category;
    private List<String> tags;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean isLiked;
    private Boolean isCollected;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    /**
     * 用户基本信息内部类
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
