package com.ai_hub.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新帖子加精状态请求
 */
@Data
public class UpdatePostEssenceRequest {
    
    /**
     * 是否加精 (true:加精, false:取消加精)
     */
    @NotNull(message = "加精状态不能为空")
    private Boolean essence;
}
