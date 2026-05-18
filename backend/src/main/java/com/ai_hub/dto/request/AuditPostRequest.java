package com.ai_hub.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审核帖子请求
 */
@Data
public class AuditPostRequest {
    
    /**
     * 审核状态 (1:通过, 0:驳回/维持审核中, 2:删除)
     */
    @NotNull(message = "状态不能为空")
    private Integer status;
    
    /**
     * 审核原因（可选）
     */
    private String reason;
}
