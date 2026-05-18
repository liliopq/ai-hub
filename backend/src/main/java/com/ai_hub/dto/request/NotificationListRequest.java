package com.ai_hub.dto.request;

import lombok.Data;

/**
 * 通知列表查询请求
 */
@Data
public class NotificationListRequest {
    
    /**
     * 页码，默认1
     */
    private Integer page = 1;
    
    /**
     * 每页条数，默认10
     */
    private Integer size = 10;
    
    /**
     * 是否已读筛选（0: 未读, 1: 已读, null: 全部）
     */
    private Integer isRead;
}
