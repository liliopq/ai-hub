package com.ai_hub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 评论点赞表
 */
@Data
@TableName("comment_like")
public class CommentLike {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 评论ID
     */
    private Long commentId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
