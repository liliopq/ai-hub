package com.ai_hub.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 帖子响应DTO
 */
@Data
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private String category;
    private String tags;
    private Long userId;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer isSticky;
    private Integer isEssence;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
