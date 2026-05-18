package com.ai_hub.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户信息响应DTO
 */
@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private String avatar;
    private String role;
    private Integer status;
    private LocalDateTime createTime;
}