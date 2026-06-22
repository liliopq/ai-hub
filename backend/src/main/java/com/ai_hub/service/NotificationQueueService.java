package com.ai_hub.service;

import com.ai_hub.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * 通知队列服务（RabbitMQ 生产者）
 * 使用 RabbitMQ 实现通知异步化处理，解耦通知产生和消费
 *
 * 消息发送流程：
 * 1. 业务层调用 sendXxxNotificationAsync() 方法
 * 2. 将 NotificationTask 序列化为 JSON 发送到 RabbitMQ
 * 3. 消息经由 Exchange 路由到 notification.queue
 * 4. 消费者异步消费消息，执行通知落库 + WebSocket 推送
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationQueueService {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送通知任务到 RabbitMQ（核心方法）
     *
     * @param task        通知任务
     * @param routingKey  路由键（格式：notification.{type}）
     */
    private void sendToQueue(NotificationTask task, String routingKey) {
        try {
            task.setCreateTime(System.currentTimeMillis());
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    routingKey,
                    task
            );
            log.debug("通知任务已发送到 RabbitMQ: type={}, userId={}, routingKey={}",
                    task.getType(), task.getUserId(), routingKey);
        } catch (Exception e) {
            log.error("发送通知任务到 RabbitMQ 失败: type={}, userId={}, error={}",
                    task.getType(), task.getUserId(), e.getMessage());
        }
    }

    /**
     * 构建通知类型对应的路由键
     * 将类型名称转为小写并拼接前缀，如 LIKE → notification.like
     *
     * @param type 通知类型
     * @return 路由键
     */
    private String buildRoutingKey(String type) {
        return RabbitMQConfig.NOTIFICATION_ROUTING_KEY_PREFIX + type.toLowerCase();
    }

    /**
     * 发送点赞通知（异步）
     *
     * @param userId       接收通知的用户ID（帖子作者）
     * @param sourceUserId 发起点赞的用户ID
     * @param postId       被点赞的帖子ID
     */
    public void sendLikeNotificationAsync(Long userId, Long sourceUserId, Long postId) {
        sendToQueue(NotificationTask.builder()
                .type("LIKE")
                .userId(userId)
                .sourceUserId(sourceUserId)
                .postId(postId)
                .build(), buildRoutingKey("LIKE"));
    }

    /**
     * 发送评论通知（异步）
     *
     * @param userId       接收通知的用户ID（帖子作者）
     * @param sourceUserId 发起评论的用户ID
     * @param postId       帖子ID
     * @param commentId    评论ID
     * @param content      评论内容（用于通知摘要）
     */
    public void sendCommentNotificationAsync(Long userId, Long sourceUserId, Long postId, Long commentId, String content) {
        sendToQueue(NotificationTask.builder()
                .type("COMMENT")
                .userId(userId)
                .sourceUserId(sourceUserId)
                .postId(postId)
                .commentId(commentId)
                .extraContent(content)
                .build(), buildRoutingKey("COMMENT"));
    }

    /**
     * 发送收藏通知（异步）
     *
     * @param userId       接收通知的用户ID（帖子作者）
     * @param sourceUserId 发起收藏的用户ID
     * @param postId       被收藏的帖子ID
     */
    public void sendCollectNotificationAsync(Long userId, Long sourceUserId, Long postId) {
        sendToQueue(NotificationTask.builder()
                .type("COLLECT")
                .userId(userId)
                .sourceUserId(sourceUserId)
                .postId(postId)
                .build(), buildRoutingKey("COLLECT"));
    }

    /**
     * 发送关注通知（异步）
     *
     * @param userId       接收通知的用户ID（被关注者）
     * @param sourceUserId 发起关注的用户ID
     */
    public void sendFollowNotificationAsync(Long userId, Long sourceUserId) {
        sendToQueue(NotificationTask.builder()
                .type("FOLLOW")
                .userId(userId)
                .sourceUserId(sourceUserId)
                .build(), buildRoutingKey("FOLLOW"));
    }

    /**
     * 发送评论点赞通知（异步）
     *
     * @param userId         接收通知的用户ID（评论作者）
     * @param sourceUserId   发起点赞的用户ID
     * @param commentId      被点赞的评论ID
     * @param commentContent 评论内容（用于通知摘要）
     */
    public void sendCommentLikeNotificationAsync(Long userId, Long sourceUserId, Long commentId, String commentContent) {
        sendToQueue(NotificationTask.builder()
                .type("COMMENT_LIKE")
                .userId(userId)
                .sourceUserId(sourceUserId)
                .commentId(commentId)
                .extraContent(commentContent)
                .build(), buildRoutingKey("COMMENT_LIKE"));
    }
}
