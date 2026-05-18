package com.ai_hub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 会话列表项响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionListItem {
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 会话标题
     */
    private String title;
    
    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdate;
}
