package com.ai_hub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 帖子创建响应DTO（简化版）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostResponse {
    private Long id;
    private String title;
    private LocalDateTime createTime;
}
