package com.ai_hub.service.impl;

import com.ai_hub.dto.response.NotificationMessage;
import com.ai_hub.entity.Notification;
import com.ai_hub.entity.User;
import com.ai_hub.mapper.NotificationMapper;
import com.ai_hub.mapper.UserMapper;
import com.ai_hub.service.NotificationQueueService;
import com.ai_hub.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * WebSocket通知发布服务实现类
 * 支持同步和异步两种模式
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketNotificationServiceImpl implements WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    private final NotificationQueueService notificationQueueService;

    @Override
    public void sendLikeNotification(Long userId, Long sourceUserId, Long postId) {
        log.info("发送点赞通知（异步），用户ID: {}, 来源用户ID: {}, 帖子ID: {}", userId, sourceUserId, postId);
        notificationQueueService.sendLikeNotificationAsync(userId, sourceUserId, postId);
    }

    @Override
    public void sendCommentNotification(Long userId, Long sourceUserId, Long postId, Long commentId, String content) {
        log.info("发送评论通知（异步），用户ID: {}, 来源用户ID: {}, 帖子ID: {}, 评论ID: {}", 
                userId, sourceUserId, postId, commentId);
        notificationQueueService.sendCommentNotificationAsync(userId, sourceUserId, postId, commentId, content);
    }

    @Override
    public void sendFollowNotification(Long userId, Long sourceUserId) {
        log.info("发送关注通知（异步），用户ID: {}, 来源用户ID: {}", userId, sourceUserId);
        notificationQueueService.sendFollowNotificationAsync(userId, sourceUserId);
    }

    @Override
    public void sendCollectNotification(Long userId, Long sourceUserId, Long postId) {
        log.info("发送收藏通知（异步），用户ID: {}, 来源用户ID: {}, 帖子ID: {}", userId, sourceUserId, postId);
        notificationQueueService.sendCollectNotificationAsync(userId, sourceUserId, postId);
    }

    @Override
    public void sendCommentLikeNotification(Long userId, Long sourceUserId, Long commentId, String commentContent) {
        log.info("发送评论点赞通知（异步），用户ID: {}, 来源用户ID: {}, 评论ID: {}", userId, sourceUserId, commentId);
        notificationQueueService.sendCommentLikeNotificationAsync(userId, sourceUserId, commentId, commentContent);
    }

    @Override
    public void sendSystemNotification(Long userId, String content) {
        log.info("发送系统通知，用户ID: {}, 内容: {}", userId, content);

        // 1. 保存通知到数据库
        Notification notification = saveNotification(userId, null, null, null, "SYSTEM", content);

        // 2. 构建通知消息
        NotificationMessage message = NotificationMessage.builder()
                .id(notification.getId())
                .type("SYSTEM")
                .content(content)
                .isRead(false)
                .createTime(notification.getCreateTime())
                .build();

        // 3. 发送WebSocket通知
        sendNotificationToUser(userId, message);
    }

    @Override
    public void sendNotificationToUser(Long userId, NotificationMessage message) {
        // 发送到用户的私有频道 /user/{userId}/notification
        String destination = "/user/" + userId + "/notification";
        messagingTemplate.convertAndSend(destination, message);
        log.info("WebSocket通知已发送，目标用户ID: {}, 频道: {}", userId, destination);
    }

    /**
     * 保存通知到数据库
     */
    private Notification saveNotification(Long userId, Long sourceUserId, Long postId, 
                                          Long commentId, String type, String content) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setSourceUserId(sourceUserId);
        notification.setSourcePostId(postId);
        notification.setSourceCommentId(commentId);
        notification.setType(type);
        notification.setContent(content);
        notification.setIsRead(0);
        
        notificationMapper.insert(notification);
        return notification;
    }

}
