package com.ai_hub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 帖子收藏记录表
 */
@Data
@TableName("post_collect")
public class PostCollect {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;
    private Long userId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
