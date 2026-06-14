package com.ai_hub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 对话消息项
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageItem {
    
    /**
     * 消息角色：USER / ASSISTANT
     */
    private String role;
    
    /**
     * 消息内容
     */
    private String content;
}
