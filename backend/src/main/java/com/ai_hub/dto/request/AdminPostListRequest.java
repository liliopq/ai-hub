package com.ai_hub.dto.request;

import lombok.Data;

/**
 * 管理员查询帖子列表请求
 */
@Data
public class AdminPostListRequest {
    
    /**
     * 页码，默认1
     */
    private Integer page = 1;
    
    /**
     * 每页条数，默认10
     */
    private Integer size = 10;
    
    /**
     * 状态筛选 (1:正常, 0:审核中, 2:已删除)
     */
    private Integer status;
    
    /**
     * 用户ID筛选
     */
    private Long userId;
}
