package com.ai_hub.service;

import com.ai_hub.dto.request.ChatRequest;
import com.ai_hub.dto.response.ChatResponse;
import com.ai_hub.dto.response.SessionListItem;

import java.util.List;

/**
 * AI 对话服务
 */
public interface AiChatService {
    
    /**
     * 与 AI 对话
     *
     * @param userId 用户ID
     * @param request 对话请求
     * @return 对话响应
     */
    ChatResponse chat(Long userId, ChatRequest request);

    /**
     * 获取用户的 AI 会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<SessionListItem> getSessions(Long userId);

    /**
     * 删除 AI 会话
     *
     * @param userId 用户ID
     * @param sessionId 会话ID
     */
    void deleteSession(Long userId, String sessionId);
}
