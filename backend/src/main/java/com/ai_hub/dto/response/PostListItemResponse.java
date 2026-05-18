package com.ai_hub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子列表项响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostListItemResponse {
    private Long id;
    private String title;
    private UserBasicInfo user;
    private String category;
    private List<String> tags;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private LocalDateTime createTime;
    
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
