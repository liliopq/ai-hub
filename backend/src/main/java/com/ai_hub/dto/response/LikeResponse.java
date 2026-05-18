package com.ai_hub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 点赞响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeResponse {
    private Integer likeCount;
    private Boolean isLiked;
}
