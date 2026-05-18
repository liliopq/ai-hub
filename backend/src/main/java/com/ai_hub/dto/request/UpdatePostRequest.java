package com.ai_hub.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新帖子请求DTO（字段可选）
 */
@Data
public class UpdatePostRequest {
    
    @Size(max = 100, message = "标题长度不能超过100个字符")
    private String title;
    
    private String content;
    
    private String category;
    
    private String[] tags;
}
