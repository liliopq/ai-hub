package com.ai_hub.service.impl;

import com.ai_hub.dto.request.ChatRequest;
import com.ai_hub.dto.response.ChatResponse;
import com.ai_hub.dto.response.MessageItem;
import com.ai_hub.dto.response.SessionListItem;
import com.ai_hub.entity.AiMessage;
import com.ai_hub.entity.AiSession;
import com.ai_hub.mapper.AiMessageMapper;
import com.ai_hub.mapper.AiSessionMapper;
import com.ai_hub.service.AiChatService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * AI 对话服务实现类
 * 支持会话上下文管理：每次对话携带最近 N 轮历史消息，让 AI 记住上下文
 * 支持流式输出（SSE）
 */
@Slf4j
@Service
public class AiChatServiceImpl implements AiChatService {

    /** 上下文窗口大小：携带最近 10 轮对话（20 条消息） */
    private static final int CONTEXT_WINDOW = 20;

    /** Redis 上下文缓存过期时间 */
    private static final int CONTEXT_CACHE_TTL = 30; // 分钟

    private final ChatClient chatClient;
    private final AiSessionMapper aiSessionMapper;
    private final AiMessageMapper aiMessageMapper;
    private final StringRedisTemplate redisTemplate;

    @Autowired
    public AiChatServiceImpl(ChatModel chatModel,
                             AiSessionMapper aiSessionMapper,
                             AiMessageMapper aiMessageMapper,
                             StringRedisTemplate redisTemplate) {
        this.chatClient = ChatClient.create(chatModel);
        this.aiSessionMapper = aiSessionMapper;
        this.aiMessageMapper = aiMessageMapper;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 与 AI 对话（流式输出）
     */
    @Override
    public void chatStream(Long userId, ChatRequest request, SseEmitter emitter) {
        log.info("AI 流式对话，用户ID: {}, 会话ID: {}", userId, request.getSessionId());

        // 将 emitter 变成 final 以便在 lambda 中使用
        final SseEmitter sseEmitter = emitter;
        
        // 设置 SSE 回调
        sseEmitter.onCompletion(() -> log.info("SSE 连接完成，会话ID: {}", request.getSessionId()));
        sseEmitter.onTimeout(() -> log.warn("SSE 连接超时，会话ID: {}", request.getSessionId()));
        sseEmitter.onError(e -> log.error("SSE 连接错误: {}", e.getMessage()));

        try {
            // 1. 处理会话ID
            String rawSessionId = request.getSessionId();
            String sessionId;
            boolean isNewSession;

            if (!StringUtils.hasText(rawSessionId)) {
                sessionId = UUID.randomUUID().toString();
                isNewSession = true;
                log.info("创建新会话，会话ID: {}", sessionId);

                // 保存新会话到数据库
                AiSession aiSession = new AiSession();
                aiSession.setUserId(userId);
                aiSession.setSessionId(sessionId);
                aiSession.setTitle(extractTitle(request.getMessage()));
                aiSession.setCreateTime(LocalDateTime.now());
                aiSession.setLastUpdate(LocalDateTime.now());
                aiSessionMapper.insert(aiSession);

                // 发送会话创建事件
                sseEmitter.send(SseEmitter.event()
                        .name("session")
                        .data("{\"sessionId\":\"" + sessionId + "\"}"));
            } else {
                sessionId = rawSessionId;
                isNewSession = false;
                updateSessionTime(sessionId);
            }

            // 2. 保存用户消息到数据库
            final String finalSessionId = sessionId;
            int nextSeq = aiMessageMapper.getMaxSeq(finalSessionId) + 1;
            AiMessage userMsg = new AiMessage();
            userMsg.setSessionId(finalSessionId);
            userMsg.setRole("USER");
            userMsg.setContent(request.getMessage());
            userMsg.setSeq(nextSeq);
            aiMessageMapper.insert(userMsg);

            // 3. 加载历史上下文
            List<AiMessage> historyMessages = loadContextMessages(finalSessionId);

            // 4. 构建带上下文的 prompt
            StringBuilder promptBuilder = new StringBuilder();
            for (AiMessage msg : historyMessages) {
                if ("USER".equals(msg.getRole())) {
                    promptBuilder.append("用户: ").append(msg.getContent()).append("\n");
                } else if ("ASSISTANT".equals(msg.getRole())) {
                    promptBuilder.append("助手: ").append(msg.getContent()).append("\n");
                }
            }
            promptBuilder.append("用户: ").append(request.getMessage()).append("\n助手: ");

            // 5. 流式调用 AI 模型
            log.info("开始流式调用 AI 模型，会话ID: {}, 上下文消息数: {}", finalSessionId, historyMessages.size());

            final StringBuilder fullResponseRef = new StringBuilder();
            
            // 使用流式 API
            chatClient.prompt()
                    .user(promptBuilder.toString())
                    .stream()
                    .content()
                    .subscribe(
                            chunk -> {
                                String content = chunk;
                                if (content != null && !content.isEmpty()) {
                                    fullResponseRef.append(content);
                                    try {
                                        // 发送 SSE 事件
                                        sseEmitter.send(SseEmitter.event()
                                                .name("content")
                                                .data(content));
                                    } catch (IOException e) {
                                        log.error("发送 SSE 事件失败: {}", e.getMessage());
                                    }
                                }
                            },
                            error -> {
                                log.error("流式调用 AI 模型错误: {}", error.getMessage());
                                try {
                                    sseEmitter.send(SseEmitter.event()
                                            .name("error")
                                            .data("AI 服务错误: " + error.getMessage()));
                                    sseEmitter.complete();
                                } catch (IOException e) {
                                    log.error("发送错误事件失败: {}", e.getMessage());
                                }
                            },
                            () -> {
                                onStreamComplete(finalSessionId, nextSeq, fullResponseRef.toString(), sseEmitter);
                            }
                    );

        } catch (Exception e) {
            log.error("流式对话处理异常: {}", e.getMessage(), e);
            try {
                sseEmitter.send(SseEmitter.event()
                        .name("error")
                        .data("处理失败: " + e.getMessage()));
                sseEmitter.completeWithError(e);
            } catch (IOException ex) {
                log.error("发送异常事件失败: {}", ex.getMessage());
            }
        }
    }

    /**
     * 流式完成回调
     */
    private void onStreamComplete(String sessionId, int nextSeq, String fullResponse, SseEmitter emitter) {
        log.info("流式调用完成，会话ID: {}, 回复长度: {}", sessionId, fullResponse.length());
        
        // 保存 AI 回复到数据库
        AiMessage assistantMsg = new AiMessage();
        assistantMsg.setSessionId(sessionId);
        assistantMsg.setRole("ASSISTANT");
        assistantMsg.setContent(fullResponse);
        assistantMsg.setSeq(nextSeq + 1);
        aiMessageMapper.insert(assistantMsg);
        
        // 更新 Redis 上下文缓存标记
        cacheContextMarker(sessionId);
        
        // 发送完成事件
        try {
            emitter.send(SseEmitter.event()
                    .name("done")
                    .data(""));
            emitter.complete();
        } catch (IOException e) {
            log.error("发送完成事件失败: {}", e.getMessage());
        }
    }

    /**
     * 与 AI 对话（带会话上下文）
     */
    @Override
    public ChatResponse chat(Long userId, ChatRequest request) {
        log.info("AI 对话，用户ID: {}, 会话ID: {}", userId, request.getSessionId());

        // 1. 处理会话ID
        String sessionId = request.getSessionId();
        boolean isNewSession = false;

        if (!StringUtils.hasText(sessionId)) {
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
            updateSessionTime(sessionId);
        }

        // 3. 保存用户消息到数据库
        int nextSeq = aiMessageMapper.getMaxSeq(sessionId) + 1;
        AiMessage userMsg = new AiMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setRole("USER");
        userMsg.setContent(request.getMessage());
        userMsg.setSeq(nextSeq);
        aiMessageMapper.insert(userMsg);

        // 4. 加载历史上下文并调用 AI 模型
        List<AiMessage> historyMessages = loadContextMessages(sessionId);
        String reply = callAiModel(request.getMessage(), historyMessages);
        log.info("AI 回复成功，会话ID: {}, 上下文消息数: {}", sessionId, historyMessages.size());

        // 5. 保存 AI 回复到数据库
        AiMessage assistantMsg = new AiMessage();
        assistantMsg.setSessionId(sessionId);
        assistantMsg.setRole("ASSISTANT");
        assistantMsg.setContent(reply);
        assistantMsg.setSeq(nextSeq + 1);
        aiMessageMapper.insert(assistantMsg);

        // 6. 更新 Redis 上下文缓存标记
        cacheContextMarker(sessionId);

        // 7. 构建响应
        return ChatResponse.builder()
                .reply(reply)
                .sessionId(sessionId)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 加载会话的最近上下文消息
     */
    private List<AiMessage> loadContextMessages(String sessionId) {
        List<AiMessage> messages = aiMessageMapper.selectRecentBySessionId(sessionId, CONTEXT_WINDOW);
        log.debug("加载会话上下文，会话ID: {}, 消息数: {}", sessionId, messages.size());
        return messages;
    }

    /**
     * 标记 Redis 上下文缓存
     */
    private void cacheContextMarker(String sessionId) {
        try {
            String cacheKey = "ai:context:" + sessionId;
            List<AiMessage> messages = aiMessageMapper.selectRecentBySessionId(sessionId, CONTEXT_WINDOW);
            redisTemplate.opsForValue().set(cacheKey, String.valueOf(messages.size()),
                    CONTEXT_CACHE_TTL, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("缓存会话上下文失败: {}", e.getMessage());
        }
    }

    /**
     * 调用 AI 模型（带历史上下文）
     */
    private String callAiModel(String currentMessage, List<AiMessage> historyMessages) {
        try {
            log.info("开始调用 AI 模型，当前消息: {}, 上下文消息数: {}",
                    currentMessage, historyMessages.size());

            // 构建带历史上下文的 prompt
            StringBuilder fullPrompt = new StringBuilder();
            
            // 添加历史消息作为上下文
            for (AiMessage msg : historyMessages) {
                if ("USER".equals(msg.getRole())) {
                    fullPrompt.append("用户: ").append(msg.getContent()).append("\n");
                } else if ("ASSISTANT".equals(msg.getRole())) {
                    fullPrompt.append("助手: ").append(msg.getContent()).append("\n");
                }
            }
            fullPrompt.append("用户: ").append(currentMessage).append("\n助手: ");

            String response = chatClient.prompt()
                    .user(fullPrompt.toString())
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
     */
    private String extractTitle(String message) {
        if (message == null || message.isEmpty()) {
            return "新对话";
        }
        String title = message.replaceAll("[\n\r]", " ").trim();
        return title.length() > 20 ? title.substring(0, 20) + "..." : title;
    }

    /**
     * 更新会话的最后更新时间
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
     */
    @Override
    public List<SessionListItem> getSessions(Long userId) {
        log.info("获取用户 AI 会话列表，用户ID: {}", userId);

        LambdaQueryWrapper<AiSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiSession::getUserId, userId)
                .orderByDesc(AiSession::getLastUpdate);

        List<AiSession> sessions = aiSessionMapper.selectList(queryWrapper);

        return sessions.stream()
                .map(session -> SessionListItem.builder()
                        .sessionId(session.getSessionId())
                        .title(session.getTitle())
                        .lastUpdate(session.getLastUpdate())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 删除 AI 会话（同时删除关联消息）
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

        // 2. 删除会话关联的消息
        int deletedMessages = aiMessageMapper.deleteBySessionId(sessionId);
        log.info("删除关联消息 {} 条", deletedMessages);

        // 3. 删除会话本身
        aiSessionMapper.deleteById(session.getId());

        // 4. 清除 Redis 缓存
        try {
            redisTemplate.delete("ai:context:" + sessionId);
        } catch (Exception e) {
            log.warn("清除 Redis 缓存失败: {}", e.getMessage());
        }

        log.info("AI 会话删除成功，会话ID: {}", sessionId);
    }

    /**
     * 获取会话的历史消息
     */
    @Override
    public List<MessageItem> getSessionMessages(Long userId, String sessionId) {
        log.info("获取会话历史消息，用户ID: {}, 会话ID: {}", userId, sessionId);

        // 1. 验证会话是否存在且属于该用户
        LambdaQueryWrapper<AiSession> sessionQuery = new LambdaQueryWrapper<>();
        sessionQuery.eq(AiSession::getUserId, userId)
                .eq(AiSession::getSessionId, sessionId);

        AiSession session = aiSessionMapper.selectOne(sessionQuery);
        if (session == null) {
            throw new RuntimeException("会话不存在或无权访问");
        }

        // 2. 查询会话的所有消息
        LambdaQueryWrapper<AiMessage> messageQuery = new LambdaQueryWrapper<>();
        messageQuery.eq(AiMessage::getSessionId, sessionId)
                .orderByAsc(AiMessage::getSeq);

        List<AiMessage> messages = aiMessageMapper.selectList(messageQuery);

        // 3. 转换为响应列表
        return messages.stream()
                .map(msg -> new MessageItem(msg.getRole(), msg.getContent()))
                .collect(Collectors.toList());
    }
}
