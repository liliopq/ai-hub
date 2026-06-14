package com.ai_hub.controller;

import com.ai_hub.dto.request.ChatRequest;
import com.ai_hub.dto.response.ChatResponse;
import com.ai_hub.dto.response.MessageItem;
import com.ai_hub.dto.response.Result;
import com.ai_hub.dto.response.SessionListItem;
import com.ai_hub.service.AiChatService;
import com.ai_hub.utils.TokenValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AI 助手控制器
 */
@Slf4j
@RequestMapping("/api/ai")
@RestController
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;
    private final TokenValidator tokenValidator;

    /** SSE 超时时间：30分钟 */
    private static final long SSE_TIMEOUT = 30 * 60 * 1000L;

    /**
     * 与 AI 对话（SSE 流式输出）
     *
     * @param authorization Authorization头
     * @param request 对话请求
     * @return SSE Emitter
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody ChatRequest request) {
        log.info("收到 AI 流式对话请求，会话ID: {}", request.getSessionId());

        // 验证Token并提取用户ID
        TokenValidator.ValidationResult result = tokenValidator.validateAndExtractUserId(authorization);
        if (!result.isSuccess()) {
            log.error("Token 验证失败: {}", result.getErrorResult().getMessage());
            SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("{\"code\":" + result.getErrorResult().getCode() + ",\"message\":\"" + result.getErrorResult().getMessage() + "\"}"));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
            return emitter;
        }

        Long userId = result.getUserId();

        // 创建 SSE Emitter
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        // 异步处理流式对话
        aiChatService.chatStream(userId, request, emitter);

        return emitter;
    }

    /**
     * 与 AI 对话（非流式版本）
     *
     * @param authorization Authorization头
     * @param request 对话请求
     * @return 对话响应
     */
    @PostMapping("/chat")
    public Result<ChatResponse> chat(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody ChatRequest request) {
        log.info("收到 AI 对话请求");

        // 验证Token并提取用户ID
        TokenValidator.ValidationResult result = tokenValidator.validateAndExtractUserId(authorization);
        if (!result.isSuccess()) {
            return Result.error(result.getErrorResult().getCode(), result.getErrorResult().getMessage());
        }

        Long userId = result.getUserId();

        // 执行对话
        ChatResponse response = aiChatService.chat(userId, request);

        return Result.success(response);
    }

    /**
     * 获取用户的 AI 会话列表
     *
     * @param authorization Authorization头
     * @return 会话列表
     */
    @GetMapping("/sessions")
    public Result<List<SessionListItem>> getSessions(
            @RequestHeader("Authorization") String authorization) {
        log.info("获取 AI 会话列表请求");

        // 验证Token并提取用户ID
        TokenValidator.ValidationResult result = tokenValidator.validateAndExtractUserId(authorization);
        if (!result.isSuccess()) {
            return Result.error(result.getErrorResult().getCode(), result.getErrorResult().getMessage());
        }

        Long userId = result.getUserId();

        // 获取会话列表
        List<SessionListItem> sessions = aiChatService.getSessions(userId);

        return Result.success(sessions);
    }

    /**
     * 删除 AI 会话
     *
     * @param authorization Authorization头
     * @param sessionId 会话ID
     * @return 删除结果
     */
    @DeleteMapping("/session/{sessionId}")
    public Result<Void> deleteSession(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String sessionId) {
        log.info("删除 AI 会话请求，会话ID: {}", sessionId);

        // 验证Token并提取用户ID
        TokenValidator.ValidationResult result = tokenValidator.validateAndExtractUserId(authorization);
        if (!result.isSuccess()) {
            return Result.error(result.getErrorResult().getCode(), result.getErrorResult().getMessage());
        }

        Long userId = result.getUserId();

        // 执行删除
        aiChatService.deleteSession(userId, sessionId);

        return Result.success("删除成功", null);
    }

    /**
     * 获取会话的历史消息
     *
     * @param authorization Authorization头
     * @param sessionId 会话ID
     * @return 消息列表
     */
    @GetMapping("/session/{sessionId}/messages")
    public Result<List<MessageItem>> getSessionMessages(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String sessionId) {
        log.info("获取会话历史消息请求，会话ID: {}", sessionId);

        // 验证Token并提取用户ID
        TokenValidator.ValidationResult result = tokenValidator.validateAndExtractUserId(authorization);
        if (!result.isSuccess()) {
            return Result.error(result.getErrorResult().getCode(), result.getErrorResult().getMessage());
        }

        Long userId = result.getUserId();

        // 获取历史消息
        List<MessageItem> messages = aiChatService.getSessionMessages(userId, sessionId);

        return Result.success(messages);
    }
}
