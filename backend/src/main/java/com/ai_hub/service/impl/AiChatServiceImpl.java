package com.ai_hub.service.impl;

import com.ai_hub.dto.request.ChatRequest;
import com.ai_hub.dto.response.ChatResponse;
import com.ai_hub.dto.response.SessionListItem;
import com.ai_hub.entity.AiSession;
import com.ai_hub.mapper.AiSessionMapper;
import com.ai_hub.service.AiChatService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * AI 对话服务实现类
 */
@Slf4j
@Service
public class AiChatServiceImpl implements AiChatService {

    private final ChatClient chatClient;
    private final AiSessionMapper aiSessionMapper;

    @Autowired
    public AiChatServiceImpl(ChatModel chatModel, AiSessionMapper aiSessionMapper) {
        this.chatClient = ChatClient.create(chatModel);
        this.aiSessionMapper = aiSessionMapper;
    }

    /**
     * 与 AI 对话
     *
     * @param userId 用户ID
     * @param request 对话请求
     * @return 对话响应
     */
    @Override
    public ChatResponse chat(Long userId, ChatRequest request) {
        log.info("AI 对话，用户ID: {}, 会话ID: {}", userId, request.getSessionId());

        // 1. 处理会话ID
        String sessionId = request.getSessionId();
        boolean isNewSession = false;
        
        if (!StringUtils.hasText(sessionId)) {
            // 如果没有提供会话ID，创建新的会话
            sessionId = UUID.randomUUID().toString();
            isNewSession = true;
            log.info("创建新会话，会话ID: {}", sessionId);
        }

        // 2. 如果是新会话，保存到数据库
        if (isNewSession) {
            AiSession aiSession = new AiSession();
            aiSession.setUserId(userId);
            aiSession.setSessionId(sessionId);
            aiSession.setTitle(extractTitle(request.getMessage()));
            aiSession.setCreateTime(LocalDateTime.now());
            aiSession.setLastUpdate(LocalDateTime.now());
            aiSessionMapper.insert(aiSession);
            log.info("保存新会话到数据库，会话ID: {}", sessionId);
        } else {
            // 更新现有会话的最后更新时间
            updateSessionTime(sessionId);
        }

        // 3. 调用 AI 模型获取回复
        String reply = callAiModel(request.getMessage());
        log.info("AI 回复成功，会话ID: {}", sessionId);

        // 4. 构建响应
        return ChatResponse.builder()
                .reply(reply)
                .sessionId(sessionId)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 调用 AI 模型
     *
     * @param message 用户消息
     * @return AI 回复
     */
    private String callAiModel(String message) {
        try {
            log.info("开始调用 AI 模型，消息: {}", message);
            
            // 使用 Spring AI 的 ChatClient 调用 AI 模型
            String response = chatClient.prompt()
                    .user(message)
                    .call()
                    .content();
            
            log.info("AI 模型调用成功");
            return response;
        } catch (Exception e) {
            log.error("调用 AI 模型失败，错误类型: {}, 错误信息: {}", 
                    e.getClass().getName(), e.getMessage());
            log.error("详细堆栈信息:", e);
            throw new RuntimeException("AI 服务暂时不可用，请稍后重试。错误: " + e.getMessage());
        }
    }

    /**
     * 从消息中提取会话标题（取前20个字符）
     *
     * @param message 用户消息
     * @return 会话标题
     */
    private String extractTitle(String message) {
        if (message == null || message.isEmpty()) {
            return "新对话";
        }
        // 去除换行符，取前20个字符
        String title = message.replaceAll("[\n\r]", " ").trim();
        return title.length() > 20 ? title.substring(0, 20) + "..." : title;
    }

    /**
     * 更新会话的最后更新时间
     *
     * @param sessionId 会话ID
     */
    private void updateSessionTime(String sessionId) {
        LambdaQueryWrapper<AiSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiSession::getSessionId, sessionId);
        AiSession session = aiSessionMapper.selectOne(queryWrapper);
        
        if (session != null) {
            session.setLastUpdate(LocalDateTime.now());
            aiSessionMapper.updateById(session);
        }
    }

    /**
     * 获取用户的 AI 会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    @Override
    public List<SessionListItem> getSessions(Long userId) {
        log.info("获取用户 AI 会话列表，用户ID: {}", userId);

        // 1. 查询用户的所有会话，按最后更新时间倒序
        LambdaQueryWrapper<AiSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiSession::getUserId, userId)
                .orderByDesc(AiSession::getLastUpdate);

        List<AiSession> sessions = aiSessionMapper.selectList(queryWrapper);

        // 2. 转换为响应 DTO
        return sessions.stream()
                .map(session -> SessionListItem.builder()
                        .sessionId(session.getSessionId())
                        .title(session.getTitle())
                        .lastUpdate(session.getLastUpdate())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 删除 AI 会话
     *
     * @param userId 用户ID
     * @param sessionId 会话ID
     */
    @Override
    public void deleteSession(Long userId, String sessionId) {
        log.info("删除 AI 会话，用户ID: {}, 会话ID: {}", userId, sessionId);

        // 1. 查询会话是否存在且属于该用户
        LambdaQueryWrapper<AiSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiSession::getUserId, userId)
                .eq(AiSession::getSessionId, sessionId);
        
        AiSession session = aiSessionMapper.selectOne(queryWrapper);
        if (session == null) {
            throw new RuntimeException("会话不存在或无权删除");
        }

        // 2. 执行删除
        aiSessionMapper.deleteById(session.getId());
        log.info("AI 会话删除成功，会话ID: {}", sessionId);
    }
}
