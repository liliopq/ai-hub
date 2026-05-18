package com.ai_hub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户注册响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（加密后的）
     */
    private String password;

    /**
     * 角色
     */
    private String role;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * JWT Token（注册后自动登录）
     */
    private String token;
}
