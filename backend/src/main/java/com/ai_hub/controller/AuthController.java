package com.ai_hub.controller;

import com.ai_hub.dto.request.LoginRequest;
import com.ai_hub.dto.request.RegisterRequest;
import com.ai_hub.dto.response.RegisterResponse;
import com.ai_hub.dto.response.Result;
import com.ai_hub.entity.User;
import com.ai_hub.mapper.UserMapper;
import com.ai_hub.service.UserService;
import com.ai_hub.utils.JwtTokenProvider;
import com.ai_hub.utils.TokenValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Slf4j                                     // 日志
@RequestMapping("/api/auth")                   // 请求路径
@RestController                            // RESTful 控制器, 返回 JSON
@RequiredArgsConstructor                   // 生成构造函数
public class AuthController {

    private final UserService userService;
    private final TokenValidator tokenValidator;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;
    private final UserMapper userMapper;

    /**
     * 健康检查接口（供 Docker / K8s 探活使用）
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("OK", "AI Hub Backend is running");
    }

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 响应结果
     */
    @PostMapping("/register")
    public Result<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        //@Valid注解：参数校验，@RequestBody注解：将请求体中的json数据映射为对象

        log.info("用户注册，用户名: {}", request.getUsername());
        RegisterResponse response = userService.register(request);
        return Result.success("注册成功", response);
    }

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 响应结果，包含 Access Token 和 Refresh Token
     */
    @PostMapping("/login")
    public Result<java.util.Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        //@Valid注解：参数校验，@RequestBody注解：将请求体中的json数据映射为对象

        log.info("用户登录，用户名: {}", request.getUsername());
        java.util.Map<String, String> tokens = userService.login(request);  // 调用用户服务, 获取 JWT Token
        return Result.success("登录成功", tokens);
    }

    /**
     * 用户退出登录（将 Token 加入黑名单）
     *
     * @param authorization Authorization 请求头
     * @return 响应结果
     */
    //在 HTTP 请求头中，Authorization 字段的值需要加上 Bearer 前缀和一个空格。
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        log.info("用户退出登录");
        
        // 验证Token并提取用户ID
        TokenValidator.ValidationResult result = tokenValidator.validateAndExtractUserId(authorization);
        if (!result.isSuccess()) {
            return Result.error(result.getErrorResult().getCode(), result.getErrorResult().getMessage());
        }
        
        // 提取 Token（去除 Bearer 前缀）
        String token = authorization.substring(7);
        
        // 将 Token 加入黑名单
        userService.logout(token);

        return Result.success("退出成功", null);
    }

    /**
     * 刷新Access Token
     *
     * @param refreshToken 刷新令牌
     * @return 响应结果，包含新的Access Token和Refresh Token
     */
    @PostMapping("/refresh")
    public Result<java.util.Map<String, String>> refresh(@RequestBody java.util.Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || refreshToken.isEmpty()) {
            return Result.error(com.ai_hub.enums.ErrorCode.BAD_REQUEST.getCode(), "刷新令牌不能为空");
        }

        log.info("收到Token刷新请求");

        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            return Result.error(com.ai_hub.enums.ErrorCode.UNAUTHORIZED.getCode(), "刷新令牌无效或已过期");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);

        // 校验 Token 版本号
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(com.ai_hub.enums.ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在");
        }
        int dbTokenVersion = user.getTokenVersion() != null ? user.getTokenVersion() : 0;
        Integer tokenVersionFromRefresh = jwtTokenProvider.getTokenVersionFromToken(refreshToken);
        int tokenVersionInToken = tokenVersionFromRefresh != null ? tokenVersionFromRefresh : 0;
        if (tokenVersionInToken != dbTokenVersion) {
            log.warn("Refresh Token 版本号不匹配，用户ID: {}, Token版本: {}, 数据库版本: {}", userId, tokenVersionInToken, dbTokenVersion);
            // 清理 Redis 中的旧 Refresh Token
            redisTemplate.delete("jwt:refresh:" + userId);
            return Result.error(com.ai_hub.enums.ErrorCode.UNAUTHORIZED.getCode(), "密码已修改，请重新登录");
        }

        String storedRefreshToken = redisTemplate.opsForValue().get("jwt:refresh:" + userId);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            log.warn("Refresh Token 不在 Redis 中或已失效，用户ID: {}", userId);
            return Result.error(com.ai_hub.enums.ErrorCode.UNAUTHORIZED.getCode(), "刷新令牌已失效，请重新登录");
        }

        String newAccessToken = jwtTokenProvider.generateToken(userId, username, dbTokenVersion);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId, username, dbTokenVersion);

        redisTemplate.opsForValue().set("jwt:refresh:" + userId, newRefreshToken,
                jwtTokenProvider.getRefreshTokenExpiration(), java.util.concurrent.TimeUnit.MILLISECONDS);

        java.util.Map<String, String> tokens = new java.util.HashMap<>();
        tokens.put("accessToken", newAccessToken);
        tokens.put("refreshToken", newRefreshToken);

        log.info("Token刷新成功，用户ID: {}", userId);

        return Result.success("Token刷新成功", tokens);
    }
}
