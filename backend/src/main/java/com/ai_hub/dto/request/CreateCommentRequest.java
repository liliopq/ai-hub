package com.ai_hub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发表评论请求DTO
 */
@Data
public class CreateCommentRequest {
    
    /**
     * 帖子ID
     */
    @NotNull(message = "帖子ID不能为空")
    private Long postId;
    
    /**
     * 父评论ID（0表示顶层评论，非0为回复某评论）
     */
    @NotNull(message = "父评论ID不能为空")
    private Long parentId;
    
    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容长度不能超过500个字符")
    private String content;
}
