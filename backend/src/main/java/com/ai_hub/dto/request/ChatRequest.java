package com.ai_hub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI 对话请求
 */
@Data
public class ChatRequest {
    
    /**
     * 用户消息
     */
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 2000, message = "消息内容不能超过2000个字符")
    private String message;
    
    /**
     * 会话ID（可选，不传则自动创建新会话）
     */
    private String sessionId;
}
