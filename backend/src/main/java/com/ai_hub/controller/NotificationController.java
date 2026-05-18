package com.ai_hub.controller;

import com.ai_hub.dto.request.NotificationListRequest;
import com.ai_hub.dto.response.NotificationResponse;
import com.ai_hub.dto.response.PageResult;
import com.ai_hub.dto.response.Result;
import com.ai_hub.dto.response.UnreadCountResponse;
import com.ai_hub.service.NotificationService;
import com.ai_hub.utils.TokenValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 通知控制器
 */
@Slf4j
@RequestMapping("/api/notification")
@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final TokenValidator tokenValidator;

    /**
     * 获取当前用户的通知列表（分页）
     *
     * @param authorization Authorization头
     * @param page 页码，默认1
     * @param size 每页条数，默认10
     * @param isRead 是否已读筛选（0: 未读, 1: 已读）
     * @return 分页结果
     */
    @GetMapping("/list")
    public Result<PageResult<NotificationResponse>> getNotifications(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer isRead) {
        log.info("获取通知列表请求，页码: {}, 每页条数: {}, 已读状态: {}", page, size, isRead);

        // 验证Token并提取用户ID
        TokenValidator.ValidationResult result = tokenValidator.validateAndExtractUserId(authorization);
        if (!result.isSuccess()) {
            return Result.error(result.getErrorResult().getCode(), result.getErrorResult().getMessage());
        }

        Long userId = result.getUserId();

        // 构建查询请求
        NotificationListRequest request = new NotificationListRequest();
        request.setPage(page);
        request.setSize(size);
        request.setIsRead(isRead);

        // 获取通知列表
        PageResult<NotificationResponse> notifications = notificationService.getNotifications(userId, request);

        return Result.success(notifications);
    }

    /**
     * 标记单个通知为已读
     *
     * @param authorization Authorization头
     * @param notificationId 通知ID
     * @return 操作结果
     */
    @PutMapping("/read/{notificationId}")
    public Result<Void> markAsRead(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long notificationId) {
        log.info("标记通知为已读请求，通知ID: {}", notificationId);

        // 验证Token并提取用户ID
        TokenValidator.ValidationResult result = tokenValidator.validateAndExtractUserId(authorization);
        if (!result.isSuccess()) {
            return Result.error(result.getErrorResult().getCode(), result.getErrorResult().getMessage());
        }

        Long userId = result.getUserId();

        // 执行标记已读
        notificationService.markAsRead(userId, notificationId);

        return Result.success("标记成功", null);
    }

    /**
     * 标记所有通知为已读
     *
     * @param authorization Authorization头
     * @return 操作结果
     */
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(
            @RequestHeader("Authorization") String authorization) {
        log.info("标记所有通知为已读请求");

        // 验证Token并提取用户ID
        TokenValidator.ValidationResult result = tokenValidator.validateAndExtractUserId(authorization);
        if (!result.isSuccess()) {
            return Result.error(result.getErrorResult().getCode(), result.getErrorResult().getMessage());
        }

        Long userId = result.getUserId();

        // 执行全部标记已读
        notificationService.markAllAsRead(userId);

        return Result.success("全部标记成功", null);
    }

    /**
     * 获取未读通知数量
     *
     * @param authorization Authorization头
     * @return 未读通知数量
     */
    @GetMapping("/unread-count")
    public Result<UnreadCountResponse> getUnreadCount(
            @RequestHeader("Authorization") String authorization) {
        log.info("获取未读通知数量请求");

        // 验证Token并提取用户ID
        TokenValidator.ValidationResult result = tokenValidator.validateAndExtractUserId(authorization);
        if (!result.isSuccess()) {
            return Result.error(result.getErrorResult().getCode(), result.getErrorResult().getMessage());
        }

        Long userId = result.getUserId();

        // 获取未读数量
        Long count = notificationService.getUnreadCount(userId);

        // 构建响应
        UnreadCountResponse response = UnreadCountResponse.builder()
                .count(count)
                .build();

        return Result.success(response);
    }
}
