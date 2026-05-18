package com.ai_hub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论树节点响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentTreeNode {
    
    /**
     * 评论ID
     */
    private Long id;
    
    /**
     * 评论内容
     */
    private String content;
    
    /**
     * 评论用户信息
     */
    private UserBasicInfo user;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 回复列表（子评论）
     */
    private List<CommentTreeNode> replies;
    
    /**
     * 用户基本信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserBasicInfo {
        private Long id;
        private String username;
        private String avatar;
    }
}
