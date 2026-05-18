package com.ai_hub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 通知表
 */
@Data
@TableName("notification")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String type;        // COMMENT, LIKE, FOLLOW, SYSTEM
    private Long sourceUserId;
    private Long sourcePostId;
    private Long sourceCommentId;
    private String content;
    private Integer isRead;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}