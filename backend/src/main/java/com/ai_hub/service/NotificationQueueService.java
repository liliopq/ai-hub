package com.ai_hub.service;

import com.ai_hub.dto.response.NotificationMessage;
import com.ai_hub.entity.Notification;
import com.ai_hub.entity.User;
import com.ai_hub.mapper.NotificationMapper;
import com.ai_hub.mapper.UserMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 通知队列服务
 * 使用 Redis Stream 实现通知异步化处理，解耦通知产生和消费
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationQueueService {

    /** Redis 通知队列 Key */
    private static final String NOTIFICATION_QUEUE_KEY = "notification:queue";

    /** Redis 消费者组 */
    private static final String CONSUMER_GROUP = "notification-consumers";

    /** 队列任务超时时间 */
    private static final int TASK_TIMEOUT_MINUTES = 5;

    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    /**
     * 初始化消费者组
     */
    @PostConstruct
    public void initConsumerGroup() {
        try {
            // 删除旧的 key，确保类型正确
            redisTemplate.delete(NOTIFICATION_QUEUE_KEY);
            log.info("清理旧的通知队列 key 完成");
        } catch (Exception e) {
            log.warn("清理旧 key 时出错，忽略: {}", e.getMessage());
        }
    }

    /**
     * 添加通知任务到队列（异步）
     *
     * @param task 通知任务
     */
    public void enqueueNotification(NotificationTask task) {
        try {
            task.setCreateTime(System.currentTimeMillis());
            String taskJson = objectMapper.writeValueAsString(task);
            redisTemplate.opsForList().rightPush(NOTIFICATION_QUEUE_KEY, taskJson);
            log.debug("通知任务已加入队列: type={}, userId={}", task.getType(), task.getUserId());
        } catch (JsonProcessingException e) {
            log.error("序列化通知任务失败: {}", e.getMessage());
        }
    }

    /**
     * 启动通知队列消费者
     * 使用 Redis List 的 BRPOP 实现阻塞拉取
     */
    @PostConstruct
    public void startConsumer() {
        Thread consumerThread = new Thread(this::consumeNotifications, "notification-consumer");
        consumerThread.setDaemon(true);
        consumerThread.start();
        log.info("通知队列消费者已启动");
    }

    /**
     * 消费通知队列
     */
    private void consumeNotifications() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // 先检查队列是否有数据，有再拉取
                Long size = redisTemplate.opsForList().size(NOTIFICATION_QUEUE_KEY);
                if (size == null || size == 0) {
                    Thread.sleep(1000);
                    continue;
                }
                
                String taskJson = redisTemplate.opsForList().leftPop(NOTIFICATION_QUEUE_KEY);
                
                if (taskJson == null) {
                    Thread.sleep(500);
                    continue;
                }

                NotificationTask task = objectMapper.readValue(taskJson, NotificationTask.class);
                processNotification(task);
                
            } catch (JsonProcessingException e) {
                log.error("反序列化通知任务失败: {}", e.getMessage(), e);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("消费通知任务失败: {}", e.getMessage(), e);
                // 等待一段时间后重试
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        log.warn("通知队列消费者已停止");
    }

    /**
     * 处理通知任务
     *
     * @param task 通知任务
     */
    private void processNotification(NotificationTask task) {
        log.info("处理通知任务: type={}, userId={}, sourceUserId={}", 
                task.getType(), task.getUserId(), task.getSourceUserId());

        try {
            // 获取来源用户信息
            User sourceUser = userMapper.selectById(task.getSourceUserId());
            if (sourceUser == null) {
                log.warn("来源用户不存在，用户ID: {}", task.getSourceUserId());
                return;
            }

            // 根据通知类型构建通知内容并保存
            String content = buildNotificationContent(task, sourceUser);
            Notification notification = saveNotification(task, content);
            NotificationMessage message = buildNotificationMessage(notification, sourceUser);

            // 发送 WebSocket 通知
            sendWebSocketNotification(task.getUserId(), message);
            
            log.info("通知发送成功: userId={}, type={}", task.getUserId(), task.getType());
            
        } catch (Exception e) {
            log.error("处理通知失败: type={}, userId={}, error={}", 
                    task.getType(), task.getUserId(), e.getMessage());
        }
    }

    /**
     * 构建通知内容
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
                yield username + " 评论了你的帖子: " + content;
            }
            case "COMMENT_LIKE" -> {
                String content = task.getExtraContent();
                if (content != null && content.length() > 50) {
                    content = content.substring(0, 50) + "...";
                }
                yield username + " 点赞了你的评论: " + content;
            }
            case "COLLECT" -> username + " 收藏了你的帖子";
            case "FOLLOW" -> username + " 关注了你";
            case "SYSTEM" -> task.getExtraContent();
            default -> username + " 与你有了新互动";
        };
    }

    /**
     * 保存通知到数据库
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
     * 构建通知消息
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
     * 发送 WebSocket 通知
     */
    private void sendWebSocketNotification(Long userId, NotificationMessage message) {
        String destination = "/user/" + userId + "/notification";
        messagingTemplate.convertAndSend(destination, message);
        log.debug("WebSocket 通知已发送: userId={}, destination={}", userId, destination);
    }

    /**
     * 发送点赞通知（异步）
     */
    public void sendLikeNotificationAsync(Long userId, Long sourceUserId, Long postId) {
        enqueueNotification(NotificationTask.builder()
                .type("LIKE")
                .userId(userId)
                .sourceUserId(sourceUserId)
                .postId(postId)
                .build());
    }

    /**
     * 发送评论通知（异步）
     */
    public void sendCommentNotificationAsync(Long userId, Long sourceUserId, Long postId, Long commentId, String content) {
        enqueueNotification(NotificationTask.builder()
                .type("COMMENT")
                .userId(userId)
                .sourceUserId(sourceUserId)
                .postId(postId)
                .commentId(commentId)
                .extraContent(content)
                .build());
    }

    /**
     * 发送收藏通知（异步）
     */
    public void sendCollectNotificationAsync(Long userId, Long sourceUserId, Long postId) {
        enqueueNotification(NotificationTask.builder()
                .type("COLLECT")
                .userId(userId)
                .sourceUserId(sourceUserId)
                .postId(postId)
                .build());
    }

    /**
     * 发送关注通知（异步）
     */
    public void sendFollowNotificationAsync(Long userId, Long sourceUserId) {
        enqueueNotification(NotificationTask.builder()
                .type("FOLLOW")
                .userId(userId)
                .sourceUserId(sourceUserId)
                .build());
    }

    /**
     * 发送评论点赞通知（异步）
     */
    public void sendCommentLikeNotificationAsync(Long userId, Long sourceUserId, Long commentId, String commentContent) {
        enqueueNotification(NotificationTask.builder()
                .type("COMMENT_LIKE")
                .userId(userId)
                .sourceUserId(sourceUserId)
                .commentId(commentId)
                .extraContent(commentContent)
                .build());
    }
}
