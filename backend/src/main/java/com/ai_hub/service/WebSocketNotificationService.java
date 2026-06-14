package com.ai_hub.service;

import com.ai_hub.dto.response.NotificationMessage;

/**
 * WebSocket通知发布服务
 */
public interface WebSocketNotificationService {

    /**
     * 发送点赞通知
     *
     * @param userId 接收通知的用户ID
     * @param sourceUserId 发起点赞的用户ID
     * @param postId 被点赞的帖子ID
     */
    void sendLikeNotification(Long userId, Long sourceUserId, Long postId);

    /**
     * 发送评论通知
     *
     * @param userId 接收通知的用户ID
     * @param sourceUserId 发起评论的用户ID
     * @param postId 帖子ID
     * @param commentId 评论ID
     * @param content 评论内容
     */
    void sendCommentNotification(Long userId, Long sourceUserId, Long postId, Long commentId, String content);

    /**
     * 发送关注通知
     *
     * @param userId 接收通知的用户ID
     * @param sourceUserId 发起关注的用户ID
     */
    void sendFollowNotification(Long userId, Long sourceUserId);

    /**
     * 发送收藏通知
     *
     * @param userId 接收通知的用户ID
     * @param sourceUserId 发起收藏的用户ID
     * @param postId 被收藏的帖子ID
     */
    void sendCollectNotification(Long userId, Long sourceUserId, Long postId);

    /**
     * 发送评论点赞通知
     *
     * @param userId 接收通知的用户ID（评论作者）
     * @param sourceUserId 发起点赞的用户ID
     * @param commentId 被点赞的评论ID
     * @param commentContent 评论内容（用于显示）
     */
    void sendCommentLikeNotification(Long userId, Long sourceUserId, Long commentId, String commentContent);

    /**
     * 发送系统通知
     *
     * @param userId 接收通知的用户ID
     * @param content 通知内容
     */
    void sendSystemNotification(Long userId, String content);

    /**
     * 发送通知消息给指定用户
     *
     * @param userId 用户ID
     * @param message 通知消息
     */
    void sendNotificationToUser(Long userId, NotificationMessage message);
}
