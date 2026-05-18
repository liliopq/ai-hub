package com.ai_hub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI 对话会话表
 */
@Data
@TableName("ai_session")
public class AiSession {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String sessionId;
    private String title;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime lastUpdate;
}