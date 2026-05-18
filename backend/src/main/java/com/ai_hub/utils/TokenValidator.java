package com.ai_hub.utils;

import com.ai_hub.dto.response.Result;
import com.ai_hub.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Token 验证工具类
 * 用于统一处理 JWT Token 的验证和用户ID提取
 */
@Component
@RequiredArgsConstructor
public class TokenValidator {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 验证 Authorization 头并提取用户ID
     *
     * @param authorization Authorization 头信息
     * @return 如果验证成功返回用户ID，失败返回错误响应
     */
    public ValidationResult validateAndExtractUserId(String authorization) {
        // 验证Authorization头是否存在且格式正确
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ValidationResult.error(Result.error(ErrorCode.BAD_REQUEST, "无效的授权头信息"));
        }

        // 提取 Token（去除 Bearer 前缀）
        String token = authorization.substring(7);

        // 验证token是否有效
        if (!jwtTokenProvider.validateToken(token)) {
            return ValidationResult.error(Result.error(ErrorCode.UNAUTHORIZED, "Token无效或已过期"));
        }

        // 从token中获取用户ID
        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        return ValidationResult.success(userId);
    }

    /**
     * 验证结果内部类
     */
    public static class ValidationResult {
        private final boolean success;
        private final Long userId;
        private final Result<?> errorResult;

        private ValidationResult(boolean success, Long userId, Result<?> errorResult) {
            this.success = success;
            this.userId = userId;
            this.errorResult = errorResult;
        }

        public static ValidationResult success(Long userId) {
            return new ValidationResult(true, userId, null);
        }

        public static ValidationResult error(Result<?> errorResult) {
            return new ValidationResult(false, null, errorResult);
        }

        public boolean isSuccess() {
            return success;
        }

        public Long getUserId() {
            return userId;
        }

        public Result<?> getErrorResult() {
            return errorResult;
        }
    }
}
