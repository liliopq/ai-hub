package com.ai_hub.controller;

import com.ai_hub.dto.request.CommentListRequest;
import com.ai_hub.dto.request.CreateCommentRequest;
import com.ai_hub.dto.response.*;
import com.ai_hub.mapper.UserMapper;
import com.ai_hub.service.CommentService;
import com.ai_hub.utils.TokenValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 评论控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {
    
    private final CommentService commentService;
    private final TokenValidator tokenValidator;
    private final UserMapper userMapper;
    
    /**
     * 发表评论（或回复评论）
     *
     * @param authorization Authorization头（必需）
     * @param request 评论请求
     * @return 评论响应
     */
    @PostMapping
    public Result<CommentResponse> createComment(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody CreateCommentRequest request) {
        
        log.info("发表评论请求，帖子ID: {}, 父评论ID: {}", request.getPostId(), request.getParentId());
        
        // 验证Token并提取用户ID
        TokenValidator.ValidationResult result = tokenValidator.validateAndExtractUserId(authorization);
        if (!result.isSuccess()) {
            return Result.error(result.getErrorResult().getCode(), result.getErrorResult().getMessage());
        }
        
        Long userId = result.getUserId();
        
        // 发表评论
        CommentResponse response = commentService.createComment(userId, request);
        
        return Result.success("评论成功", response);
    }
    
    /**
     * 获取帖子的评论树
     *
     * @param postId 帖子ID
     * @param page 页码（可选，默认1）
     * @param size 每页条数（可选，默认20）
     * @return 评论树分页结果
     */
    @GetMapping("/list/{postId}")
    public Result<PageResult<CommentTreeNode>> getCommentTree(
            @PathVariable Long postId,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        
        log.info("获取帖子评论树，帖子ID: {}, 页码: {}, 每页条数: {}", postId, page, size);
        
        // 构建请求对象
        CommentListRequest request = new CommentListRequest();
        request.setPage(page);
        request.setSize(size);
        
        // 获取评论树
        PageResult<CommentTreeNode> result = commentService.getCommentTree(postId, request);
        
        return Result.success("success", result);
    }
    
    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @param authorization Authorization头（必需）
     * @return 标准成功响应
     */
    @DeleteMapping("/{commentId}")
    public Result<Void> deleteComment(
            @PathVariable Long commentId,
            @RequestHeader("Authorization") String authorization) {
        
        log.info("删除评论，评论ID: {}", commentId);
        
        // 验证Token并提取用户ID
        TokenValidator.ValidationResult result = tokenValidator.validateAndExtractUserId(authorization);
        if (!result.isSuccess()) {
            return Result.error(result.getErrorResult().getCode(), result.getErrorResult().getMessage());
        }
        
        Long userId = result.getUserId();
        
        // 查询用户角色
        com.ai_hub.entity.User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(com.ai_hub.enums.ErrorCode.USER_NOT_FOUND);
        }
        
        // 删除评论
        commentService.deleteComment(commentId, userId, user.getRole());
        
        return Result.success("删除成功", null);
    }
    
    /**
     * 点赞/取消点赞评论
     *
     * @param commentId 评论ID
     * @param authorization Authorization头（必需）
     * @return 点赞响应
     */
    @PostMapping("/{commentId}/like")
    public Result<CommentLikeResponse> likeComment(
            @PathVariable Long commentId,
            @RequestHeader("Authorization") String authorization) {
        
        log.info("点赞评论请求，评论ID: {}", commentId);
        
        // 验证Token并提取用户ID
        TokenValidator.ValidationResult result = tokenValidator.validateAndExtractUserId(authorization);
        if (!result.isSuccess()) {
            return Result.error(result.getErrorResult().getCode(), result.getErrorResult().getMessage());
        }
        
        Long userId = result.getUserId();
        
        // 点赞/取消点赞
        CommentLikeResponse response = commentService.likeComment(commentId, userId);
        
        return Result.success("success", response);
    }
}
