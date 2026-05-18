package com.ai_hub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户关注表
 */
@Data
@TableName("user_follow")
public class UserFollow {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long followerId;
    private Long followeeId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}