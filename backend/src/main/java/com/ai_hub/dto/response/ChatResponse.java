package com.ai_hub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 对话响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    
    /**
     * AI 回复内容
     */
    private String reply;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 时间戳
     */
    private LocalDateTime timestamp;
}
