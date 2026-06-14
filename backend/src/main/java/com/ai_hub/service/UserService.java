package com.ai_hub.service;

import com.ai_hub.dto.request.ChangePasswordRequest;
import com.ai_hub.dto.request.LoginRequest;
import com.ai_hub.dto.request.RegisterRequest;
import com.ai_hub.dto.request.UpdateUserRequest;
import com.ai_hub.dto.response.RegisterResponse;
import com.ai_hub.dto.response.UserResponse;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 注册响应
     */
    RegisterResponse register(RegisterRequest request);

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 包含 Access Token 和 Refresh Token 的 Map
     */
    java.util.Map<String, String> login(LoginRequest request);

    /**
     * 用户退出登录（将 Token 加入黑名单）
     *
     * @param token JWT Token
     */
    void logout(String token);

    /**
     * 获取当前用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserResponse getCurrentUser(Long userId);

    /**
     * 更新用户信息
     *
     * @param userId 用户ID
     * @param request 更新请求
     * @return 更新后的用户信息
     */
    UserResponse updateUser(Long userId, UpdateUserRequest request);

    /**
     * 修改密码
     *
     * @param userId 用户ID
     * @param request 修改密码请求
     */
    void changePassword(Long userId, ChangePasswordRequest request);

    /**
     * 获取指定用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserResponse getUserById(Long userId);
}
