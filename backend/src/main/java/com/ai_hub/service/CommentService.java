package com.ai_hub.service;

import com.ai_hub.dto.request.CommentListRequest;
import com.ai_hub.dto.request.CreateCommentRequest;
import com.ai_hub.dto.response.CommentLikeResponse;
import com.ai_hub.dto.response.CommentResponse;
import com.ai_hub.dto.response.CommentTreeNode;
import com.ai_hub.dto.response.PageResult;

/**
 * 评论服务接口
 */
public interface CommentService {
    
    /**
     * 发表评论（或回复评论）
     *
     * @param userId 当前用户ID
     * @param request 评论请求
     * @return 评论响应
     */
    CommentResponse createComment(Long userId, CreateCommentRequest request);
    
    /**
     * 获取帖子的评论树
     *
     * @param postId 帖子ID
     * @param request 分页请求
     * @return 评论树分页结果
     */
    PageResult<CommentTreeNode> getCommentTree(Long postId, CommentListRequest request);
    
    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @param userId 当前用户ID
     * @param userRole 当前用户角色
     */
    void deleteComment(Long commentId, Long userId, String userRole);
    
    /**
     * 点赞/取消点赞评论
     *
     * @param commentId 评论ID
     * @param userId 当前用户ID
     * @return 点赞响应
     */
    CommentLikeResponse likeComment(Long commentId, Long userId);
}
