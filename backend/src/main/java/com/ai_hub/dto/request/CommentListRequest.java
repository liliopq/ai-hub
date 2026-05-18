package com.ai_hub.dto.request;

import lombok.Data;

/**
 * 评论列表请求DTO
 */
@Data
public class CommentListRequest {
    
    /**
     * 页码（默认1）
     */
    private Integer page = 1;
    
    /**
     * 每页条数（默认20）
     */
    private Integer size = 20;
}
