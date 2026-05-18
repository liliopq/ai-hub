package com.ai_hub.dto.request;

import lombok.Data;

/**
 * 帖子列表查询请求DTO
 */
@Data
public class PostListRequest {
    
    /**
     * 页码，默认1
     */
    private Integer page = 1;
    
    /**
     * 每页条数，默认10
     */
    private Integer size = 10;
    
    /**
     * 分类筛选
     */
    private String category;
    
    /**
     * 排序：time(默认) / hot(热度)
     */
    private String sortBy = "time";
    
    /**
     * 标签筛选
     */
    private String tag;
    
    /**
     * 搜索关键词（搜索标题和内容）
     */
    private String keyword;
}
