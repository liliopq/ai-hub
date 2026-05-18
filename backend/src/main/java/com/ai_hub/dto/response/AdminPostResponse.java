package com.ai_hub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理员帖子信息响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPostResponse {
    
    /**
     * 帖子ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 标题
     */
    private String title;
    
    /**
     * 内容摘要（前100个字符）
     */
    private String contentSummary;
    
    /**
     * 分类
     */
    private String category;
    
    /**
     * 标签
     */
    private String tags;
    
    /**
     * 浏览次数
     */
    private Integer viewCount;
    
    /**
     * 点赞次数
     */
    private Integer likeCount;
    
    /**
     * 收藏次数
     */
    private Integer collectCount;
    
    /**
     * 评论次数
     */
    private Integer commentCount;
    
    /**
     * 是否置顶 (0:否, 1:是)
     */
    private Integer isSticky;
    
    /**
     * 是否精华 (0:否, 1:是)
     */
    private Integer isEssence;
    
    /**
     * 状态 (1:正常, 0:审核中, 2:已删除)
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除标记 (0:未删除, 1:已删除)
     */
    private Integer deleted;
}
