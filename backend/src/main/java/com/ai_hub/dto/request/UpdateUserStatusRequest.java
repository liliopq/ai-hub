package com.ai_hub.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新用户状态请求
 */
@Data
public class UpdateUserStatusRequest {
    
    /**
     * 用户状态 (0:封禁, 1:正常)
     */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
