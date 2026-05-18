package com.ai_hub.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 点赞请求DTO
 */
@Data
public class LikeRequest {
    
    /**
     * 操作类型：like(点赞) / unlike(取消点赞)
     */
    @NotBlank(message = "操作类型不能为空")
    private String action;
}
