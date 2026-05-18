package com.ai_hub.controller;

import com.ai_hub.dto.request.ChangePasswordRequest;
import com.ai_hub.dto.request.UpdateUserRequest;
import com.ai_hub.dto.response.Result;
import com.ai_hub.dto.response.UserResponse;
import com.ai_hub.service.UserService;
import com.ai_hub.utils.TokenValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@Slf4j                                     // 日志
@RequestMapping("/api/user")                   // 请求路径
@RestController                            // RESTful 控制器, 返回 JSON
@RequiredArgsConstructor                   // 生成构造函数
public class UserController {

    private final UserService userService;
    private final TokenValidator tokenValidator;

    /**
     * 获取当前用户信息
     *
     * @param authorization Authorization头
     * @return 当前用户信息
     */
    @GetMapping("/me")
    public Result<UserResponse> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authorization) {
        log.info("获取当前用户信息");
        
        // 验证Token并提取用户ID
        TokenValidator.ValidationResult result = tokenValidator.validateAndExtractUserId(authorization);
        if (!result.isSuccess()) {
            return Result.error(result.getErrorResult().getCode(), result.getErrorResult().getMessage());
        }
        
        Long userId = result.getUserId();
        
        // 获取用户信息
        UserResponse userResponse = userService.getCurrentUser(userId);
        
        return Result.success(userResponse);
    }

    /**
     * 更新用户信息（昵称、头像等）
     *
     * @param authorization Authorization头
     * @param request 更新请求
     * @return 更新后的用户信息
     */
    @PutMapping("/me")
    public Result<UserResponse> updateUser(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody UpdateUserRequest request) {
        log.info("更新用户信息");
        
        // 验证Token并提取用户ID
        TokenValidator.ValidationResult result = tokenValidator.validateAndExtractUserId(authorization);
        if (!result.isSuccess()) {
            return Result.error(result.getErrorResult().getCode(), result.getErrorResult().getMessage());
        }
        
        Long userId = result.getUserId();
        
        // 更新用户信息
        UserResponse userResponse = userService.updateUser(userId, request);
        
        return Result.success(userResponse);
    }

    /**
     * 修改密码
     *
     * @param authorization Authorization头
     * @param request 修改密码请求
     * @return 修改结果
     */
    @PutMapping("/me/password")
    public Result<Void> changePassword(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody ChangePasswordRequest request) {
        log.info("修改密码");
        
        // 验证Token并提取用户ID
        TokenValidator.ValidationResult result = tokenValidator.validateAndExtractUserId(authorization);
        if (!result.isSuccess()) {
            return Result.error(result.getErrorResult().getCode(), result.getErrorResult().getMessage());
        }
        
        Long userId = result.getUserId();
        
        // 修改密码
        userService.changePassword(userId, request);
        
        return Result.success("密码修改成功，请重新登录", null);
    }

    /**
     * 获取指定用户信息（公开信息）
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/{userId}")
    public Result<UserResponse> getUserById(@PathVariable Long userId) {
        log.info("获取用户信息，用户ID: {}", userId);
        
        UserResponse userResponse = userService.getUserById(userId);
        
        return Result.success(userResponse);
    }
}
