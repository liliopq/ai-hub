package com.ai_hub.dto.request;

import lombok.Data;

/**
 * 更新用户信息请求DTO
 */
@Data
public class UpdateUserRequest {
    private String username;
    private String email;
    private String phoneNumber;
    private String avatar;
}