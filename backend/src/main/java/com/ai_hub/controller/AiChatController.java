package com.ai_hub.controller;

import com.ai_hub.dto.request.ChatRequest;
import com.ai_hub.dto.response.ChatResponse;
import com.ai_hub.dto.response.Result;
import com.ai_hub.dto.response.SessionListItem;
import com.ai_hub.service.AiChatService;
import com.ai_hub.utils.TokenValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 与 AI 对话
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
}
