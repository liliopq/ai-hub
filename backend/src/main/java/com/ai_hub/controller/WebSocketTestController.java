package com.ai_hub.controller;

import com.ai_hub.dto.response.NotificationMessage;
import com.ai_hub.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * WebSocket 测试控制器
 * 用于测试 WebSocket 通知功能
 */
@Slf4j
@RestController
@RequestMapping("/api/ws")
@RequiredArgsConstructor
public class WebSocketTestController {

    private final WebSocketNotificationService webSocketNotificationService;

    /**
     * 发送测试个人通知
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @PostMapping("/test/personal/{userId}")
    public String sendPersonalNotification(@PathVariable Long userId) {
        log.info("发送测试个人通知，用户ID: {}", userId);

        NotificationMessage message = NotificationMessage.builder()
                .type("SYSTEM")
                .content("这是一条测试个人通知")
                .postId(null)
                .createTime(LocalDateTime.now())
                .build();

        webSocketNotificationService.sendNotificationToUser(userId, message);

        return "个人通知已发送给用户 " + userId;
    }

    /**
     * 发送测试全局公告
     *
     * @return 操作结果
     */
    @PostMapping("/test/announcement")
    public String sendAnnouncement() {
        log.info("发送测试全局公告");

        NotificationMessage message = NotificationMessage.builder()
                .type("SYSTEM")
                .content("这是一条全局公告")
                .postId(null)
                .createTime(LocalDateTime.now())
                .build();

        // 注意：当前服务不支持群发消息，这里仅作为示例
        log.warn("全局公告功能尚未实现，消息内容: {}", message.getContent());
        // webSocketNotificationService.sendToAll(message);

        return "全局公告已发送";
    }

    /**
     * 模拟评论通知
     *
     * @param userId 被评论的用户ID
     * @param postId 帖子ID
     * @param commenterName 评论者姓名
     * @return 操作结果
     */
    @PostMapping("/test/comment/{userId}")
    public String sendCommentNotification(
            @PathVariable Long userId,
            @RequestParam Long postId,
            @RequestParam String commenterName) {
        log.info("发送评论通知，用户ID: {}, 帖子ID: {}", userId, postId);

        NotificationMessage message = NotificationMessage.builder()
                .type("COMMENT")
                .content("用户 " + commenterName + " 评论了你的帖子")
                .postId(postId)
                .createTime(LocalDateTime.now())
                .build();

        webSocketNotificationService.sendNotificationToUser(userId, message);

        return "评论通知已发送给用户 " + userId;
    }
}
