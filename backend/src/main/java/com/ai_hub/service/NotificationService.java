package com.ai_hub.service;

import com.ai_hub.dto.request.NotificationListRequest;
import com.ai_hub.dto.response.NotificationResponse;
import com.ai_hub.dto.response.PageResult;

/**
 * 通知服务
 */
public interface NotificationService {
    
    /**
     * 获取用户的通知列表（分页）
     *
     * @param userId 用户ID
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<NotificationResponse> getNotifications(Long userId, NotificationListRequest request);

    /**
     * 标记单个通知为已读
     *
     * @param userId 用户ID
     * @param notificationId 通知ID
     */
    void markAsRead(Long userId, Long notificationId);

    /**
     * 标记用户的所有通知为已读
     *
     * @param userId 用户ID
     */
    void markAllAsRead(Long userId);

    /**
     * 获取用户的未读通知数量
     *
     * @param userId 用户ID
     * @return 未读通知数量
     */
    Long getUnreadCount(Long userId);
}
