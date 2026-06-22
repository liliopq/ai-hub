package com.ai_hub.service;

import com.ai_hub.config.RabbitMQConfig;
import com.ai_hub.dto.response.NotificationMessage;
import com.ai_hub.entity.Notification;
import com.ai_hub.entity.User;
import com.ai_hub.mapper.NotificationMapper;
import com.ai_hub.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * 通知消息消费者（RabbitMQ 消费者）
 * 监听 notification.queue 队列，异步处理通知任务
 *
 * 消费流程：
 * 1. 从队列接收 NotificationTask 消息（自动反序列化）
 * 2. 查询来源用户信息
 * 3. 根据通知类型构建通知内容
 * 4. 保存通知记录到数据库
 * 5. 通过 WebSocket（STOMP）推送实时通知给目标用户
 *
 * 可靠性保障：
 * - auto ACK 模式配合重试机制（最多 3 次，间隔指数递增）
 * - 消费失败超过重试次数后，消息进入死信队列（notification.dead.queue）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationMessageConsumer {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 监听通知队列，消费通知任务
     *
     * @param task 通知任务（由 Jackson JSON 自动反序列化为 Java 对象）
     */
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void onMessage(NotificationTask task) {
        log.info("收到通知任务: type={}, userId={}, sourceUserId={}",
                task.getType(), task.getUserId(), task.getSourceUserId());

        try {
            processNotification(task);
            log.info("通知处理完成: type={}, userId={}", task.getType(), task.getUserId());
        } catch (Exception e) {
            log.error("通知处理失败: type={}, userId={}, error={}",
                    task.getType(), task.getUserId(), e.getMessage(), e);
            // 抛出异常触发 Spring Retry 重试机制
            throw new RuntimeException("通知处理失败，等待重试", e);
        }
    }

    /**
     * 处理通知任务
     *
     * @param task 通知任务
     */
    private void processNotification(NotificationTask task) {
        // 1. 获取来源用户信息
        User sourceUser = userMapper.selectById(task.getSourceUserId());
        if (sourceUser == null) {
            log.warn("来源用户不存在，跳过通知处理: sourceUserId={}", task.getSourceUserId());
            return;
        }

        // 2. 根据通知类型构建通知内容
        String content = buildNotificationContent(task, sourceUser);

        // 3. 保存通知到数据库
        Notification notification = saveNotification(task, content);

        // 4. 构建 WebSocket 消息
        NotificationMessage message = buildNotificationMessage(notification, sourceUser);

        // 5. 通过 WebSocket 推送实时通知
        sendWebSocketNotification(task.getUserId(), message);
    }

    /**
     * 根据通知类型构建通知内容文案
     *
     * @param task       通知任务
     * @param sourceUser 触发通知的用户
     * @return 通知内容文案
     */
    private String buildNotificationContent(NotificationTask task, User sourceUser) {
        String username = sourceUser.getUsername();

        return switch (task.getType()) {
            case "LIKE" -> username + " 点赞了你的帖子";
            case "COMMENT" -> {
                String content = task.getExtraContent();
                if (content != null && content.length() > 50) {
                    content = content.substring(0, 50) + "...";
                }
                yield username + " 评论了你的帖子" + (content != null ? ": " + content : "");
            }
            case "COMMENT_LIKE" -> {
                String content = task.getExtraContent();
                if (content != null && content.length() > 50) {
                    content = content.substring(0, 50) + "...";
                }
                yield username + " 点赞了你的评论" + (content != null ? ": " + content : "");
            }
            case "COLLECT" -> username + " 收藏了你的帖子";
            case "FOLLOW" -> username + " 关注了你";
            case "SYSTEM" -> task.getExtraContent();
            default -> username + " 与你有了新互动";
        };
    }

    /**
     * 保存通知记录到数据库
     *
     * @param task    通知任务
     * @param content 通知内容
     * @return 保存后的通知实体（包含自增ID）
     */
    private Notification saveNotification(NotificationTask task, String content) {
        Notification notification = new Notification();
        notification.setUserId(task.getUserId());
        notification.setSourceUserId(task.getSourceUserId());
        notification.setSourcePostId(task.getPostId());
        notification.setSourceCommentId(task.getCommentId());
        notification.setType(task.getType());
        notification.setContent(content);
        notification.setIsRead(0);

        notificationMapper.insert(notification);
        return notification;
    }

    /**
     * 构建 WebSocket 通知消息 DTO
     *
     * @param notification 通知实体
     * @param sourceUser   来源用户
     * @return WebSocket 消息 DTO
     */
    private NotificationMessage buildNotificationMessage(Notification notification, User sourceUser) {
        NotificationMessage.UserBasicInfo userInfo = NotificationMessage.UserBasicInfo.builder()
                .id(sourceUser.getId())
                .username(sourceUser.getUsername())
                .avatar(sourceUser.getAvatar())
                .build();

        return NotificationMessage.builder()
                .id(notification.getId())
                .type(notification.getType())
                .content(notification.getContent())
                .sourceUser(userInfo)
                .postId(notification.getSourcePostId())
                .commentId(notification.getSourceCommentId())
                .isRead(false)
                .createTime(notification.getCreateTime())
                .build();
    }

    /**
     * 通过 WebSocket（STOMP）推送实时通知
     * 发送到用户私有频道 /user/{userId}/notification
     *
     * @param userId  目标用户ID
     * @param message 通知消息
     */
    private void sendWebSocketNotification(Long userId, NotificationMessage message) {
        String destination = "/user/" + userId + "/notification";
        messagingTemplate.convertAndSend(destination, message);
        log.debug("WebSocket 通知已推送: userId={}, destination={}", userId, destination);
    }
}
