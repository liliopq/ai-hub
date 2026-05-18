package com.ai_hub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评论点赞响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentLikeResponse {
    
    /**
     * 点赞数
     */
    private Integer likeCount;
}
