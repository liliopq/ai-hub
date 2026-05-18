package com.ai_hub.controller;

import com.ai_hub.dto.request.LoginRequest;
import com.ai_hub.dto.request.RegisterRequest;
import com.ai_hub.dto.response.RegisterResponse;
import com.ai_hub.dto.response.Result;
import com.ai_hub.service.UserService;
import com.ai_hub.utils.TokenValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
     * @return 响应结果
     */
    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody LoginRequest request) {
        //@Valid注解：参数校验，@RequestBody注解：将请求体中的json数据映射为对象
        
        log.info("用户登录，用户名: {}", request.getUsername());
        String token = userService.login(request);
        return Result.success("登录成功", token);
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
}
