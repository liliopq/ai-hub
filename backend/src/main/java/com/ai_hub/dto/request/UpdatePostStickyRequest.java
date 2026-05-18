package com.ai_hub.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新帖子置顶状态请求
 */
@Data
public class UpdatePostStickyRequest {
    
    /**
     * 是否置顶 (true:置顶, false:取消置顶)
     */
    @NotNull(message = "置顶状态不能为空")
    private Boolean sticky;
}
