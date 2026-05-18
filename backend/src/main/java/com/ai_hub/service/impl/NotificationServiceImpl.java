package com.ai_hub.service.impl;

import com.ai_hub.dto.request.NotificationListRequest;
import com.ai_hub.dto.response.NotificationResponse;
import com.ai_hub.dto.response.PageResult;
import com.ai_hub.entity.Notification;
import com.ai_hub.entity.User;
import com.ai_hub.mapper.NotificationMapper;
import com.ai_hub.mapper.UserMapper;
import com.ai_hub.service.NotificationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;

    /**
     * 获取用户的通知列表（分页）
     *
     * @param userId 用户ID
     * @param request 查询请求
     * @return 分页结果
     */
    @Override
    public PageResult<NotificationResponse> getNotifications(Long userId, NotificationListRequest request) {
        log.info("获取用户通知列表，用户ID: {}, 页码: {}, 每页条数: {}, 已读状态: {}",
                userId, request.getPage(), request.getSize(), request.getIsRead());

        // 1. 构建查询条件
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getUserId, userId);
        
        // 按是否已读筛选
        if (request.getIsRead() != null) {
            queryWrapper.eq(Notification::getIsRead, request.getIsRead());
        }
        
        // 按创建时间倒序
        queryWrapper.orderByDesc(Notification::getCreateTime);

        // 2. 分页查询
        Page<Notification> page = new Page<>(request.getPage(), request.getSize());
        Page<Notification> notificationPage = notificationMapper.selectPage(page, queryWrapper);

        // 3. 转换为响应 DTO
        List<NotificationResponse> records = notificationPage.getRecords().stream()
                .map(this::convertToNotificationResponse)
                .collect(Collectors.toList());

        // 4. 构建分页结果
        return PageResult.of(
                records,
                notificationPage.getTotal(),
                notificationPage.getSize(),
                notificationPage.getCurrent()
        );
    }

    /**
     * 将 Notification 实体转换为 NotificationResponse
     *
     * @param notification 通知实体
     * @return 通知响应 DTO
     */
    private NotificationResponse convertToNotificationResponse(Notification notification) {
        NotificationResponse response = NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .content(notification.getContent())
                .postId(notification.getSourcePostId())
                .isRead(notification.getIsRead() == 1)
                .createTime(notification.getCreateTime())
                .build();

        // 查询来源用户信息
        if (notification.getSourceUserId() != null) {
            User sourceUser = userMapper.selectById(notification.getSourceUserId());
            if (sourceUser != null) {
                NotificationResponse.UserBasicInfo userInfo = NotificationResponse.UserBasicInfo.builder()
                        .id(sourceUser.getId())
                        .username(sourceUser.getUsername())
                        .avatar(sourceUser.getAvatar())
                        .build();
                response.setSourceUser(userInfo);
            }
        }

        return response;
    }

    /**
     * 标记单个通知为已读
     *
     * @param userId 用户ID
     * @param notificationId 通知ID
     */
    @Override
    public void markAsRead(Long userId, Long notificationId) {
        log.info("标记通知为已读，用户ID: {}, 通知ID: {}", userId, notificationId);

        // 1. 查询通知是否存在且属于该用户
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getId, notificationId)
                .eq(Notification::getUserId, userId);
        
        Notification notification = notificationMapper.selectOne(queryWrapper);
        if (notification == null) {
            throw new RuntimeException("通知不存在或无权操作");
        }

        // 2. 如果已经是已读状态，直接返回
        if (notification.getIsRead() == 1) {
            log.info("通知已经是已读状态，无需更新");
            return;
        }

        // 3. 更新为已读
        notification.setIsRead(1);
        notificationMapper.updateById(notification);
        log.info("通知标记为已读成功，通知ID: {}", notificationId);
    }

    /**
     * 标记用户的所有通知为已读
     *
     * @param userId 用户ID
     */
    @Override
    public void markAllAsRead(Long userId) {
        log.info("标记所有通知为已读，用户ID: {}", userId);

        // 1. 查询所有未读通知
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0);
        
        List<Notification> unreadNotifications = notificationMapper.selectList(queryWrapper);
        
        if (unreadNotifications.isEmpty()) {
            log.info("没有未读通知，无需更新");
            return;
        }

        // 2. 批量更新为已读
        unreadNotifications.forEach(notification -> notification.setIsRead(1));
        
        // 逐条更新（MyBatis-Plus 没有直接的批量更新方法，这里简化处理）
        for (Notification notification : unreadNotifications) {
            notificationMapper.updateById(notification);
        }
        
        log.info("所有通知标记为已读成功，共更新 {} 条", unreadNotifications.size());
    }

    /**
     * 获取用户的未读通知数量
     *
     * @param userId 用户ID
     * @return 未读通知数量
     */
    @Override
    public Long getUnreadCount(Long userId) {
        log.info("获取未读通知数量，用户ID: {}", userId);

        // 查询未读通知数量
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0);
        
        Long count = notificationMapper.selectCount(queryWrapper);
        log.info("未读通知数量: {}", count);
        
        return count;
    }
}
