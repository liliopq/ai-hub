package com.ai_hub.controller;

import com.ai_hub.dto.request.CreatePostRequest;
import com.ai_hub.dto.request.LikeRequest;
import com.ai_hub.dto.request.PostListRequest;
import com.ai_hub.dto.request.UpdatePostRequest;
import com.ai_hub.dto.response.*;
import com.ai_hub.entity.User;
import com.ai_hub.mapper.UserMapper;
import com.ai_hub.service.PostService;
import com.ai_hub.utils.TokenValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 帖子控制器
 */
@Slf4j                                     // 日志
@RequestMapping("/api/post")                   // 请求路径
@RestController                            // RESTful 控制器, 返回 JSON
@RequiredArgsConstructor                   // 生成构造函数
public class PostController {

    private final PostService postService;
    private final TokenValidator tokenValidator;
    private final UserMapper userMapper;

    /**
     * 发布帖子
     *
     * @param authorization Authorization头
     * @param request 发布帖子请求
     * @return 响应结果
     */
    @PostMapping
    public Result<CreatePostResponse> createPost(@RequestHeader(value = "Authorization", required = false) String authorization,
                                           @Valid @RequestBody CreatePostRequest request) {
        log.info("发布帖子，标题: {}", request.getTitle());
        
        // 验证Token并提取用户ID
        TokenValidator.ValidationResult result = tokenValidator.validateAndExtractUserId(authorization);
        if (!result.isSuccess()) {
            return Result.error(result.getErrorResult().getCode(), result.getErrorResult().getMessage());
        }
        
        Long userId = result.getUserId();
        
        // 发布帖子
        CreatePostResponse postResponse = postService.createPost(userId, request);
        
        return Result.success("发布成功", postResponse);
    }

    /**
     * 分页获取帖子列表（首页/推荐）
     *
     * @param page 页码，默认1
     * @param size 每页条数，默认10
     * @param category 分类筛选
     * @param tag 标签筛选
     * @param keyword 搜索关键词
     * @param sortBy 排序：time(默认) / hot(热度)
     * @return 分页结果
     */
    @GetMapping("/list")
    public Result<PageResult<PostListItemResponse>> getPostList(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "time") String sortBy) {
        log.info("获取帖子列表，页码: {}, 每页条数: {}, 分类: {}, 标签: {}, 关键词: {}, 排序: {}",
                page, size, category, tag, keyword, sortBy);
        
        // 构建查询请求
        PostListRequest request = new PostListRequest();
        request.setPage(page);
        request.setSize(size);
        request.setCategory(category);
        request.setTag(tag);
        request.setKeyword(keyword);
        request.setSortBy(sortBy);
        
        // 获取帖子列表
        PageResult<PostListItemResponse> result = postService.getPostList(request);
        
        return Result.success(result);
    }

    /**
     * 获取帖子详情
     *
     * @param postId 帖子ID
     * @param authorization Authorization头（可选）
     * @return 帖子详情
     */
    @GetMapping("/{postId}")
    public Result<PostDetailResponse> getPostDetail(
            @PathVariable Long postId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        log.info("获取帖子详情，帖子ID: {}", postId);
        
        // 尝试从Token中获取用户ID（如果有的话）
        Long currentUserId = null;
        if (authorization != null && authorization.startsWith("Bearer ")) {
            try {
                TokenValidator.ValidationResult result = tokenValidator.validateAndExtractUserId(authorization);
                if (result.isSuccess()) {
                    currentUserId = result.getUserId();
                }
            } catch (Exception e) {
                // 如果Token无效，忽略，继续以未登录状态获取帖子详情
                log.warn("Token验证失败，将以未登录状态获取帖子详情");
            }
        }
        
        // 获取帖子详情
        PostDetailResponse postDetail = postService.getPostDetail(postId, currentUserId);
        
        return Result.success(postDetail);
    }

    /**
     * 更新帖子
     *
     * @param postId 帖子ID
     * @param authorization Authorization头（必需）
     * @param request 更新请求
     * @return 更新后的帖子详情
     */
    @PutMapping("/{postId}")
    public Result<PostDetailResponse> updatePost(
            @PathVariable Long postId,
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody UpdatePostRequest request) {
        log.info("更新帖子，帖子ID: {}", postId);
        
        // 验证Token并提取用户ID
        TokenValidator.ValidationResult result = tokenValidator.validateAndExtractUserId(authorization);
        if (!result.isSuccess()) {
            return Result.error(result.getErrorResult().getCode(), result.getErrorResult().getMessage());
        }
        
        Long userId = result.getUserId();
        
        // 查询用户角色
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(com.ai_hub.enums.ErrorCode.USER_NOT_FOUND);
        }
        
        // 更新帖子
        PostDetailResponse updatedPost = postService.updatePost(postId, userId, user.getRole(), request);
        
        return Result.success("更新成功", updatedPost);
    }

    /**
     * 删除帖子（软删除）
     *
     * @param postId 帖子ID
     * @param authorization Authorization头（必需）
     * @return 删除结果
     */
    @DeleteMapping("/{postId}")
    public Result<Void> deletePost(
            @PathVariable Long postId,
            @RequestHeader("Authorization") String authorization) {
        log.info("删除帖子，帖子ID: {}", postId);
        
        // 验证Token并提取用户ID
        TokenValidator.ValidationResult result = tokenValidator.validateAndExtractUserId(authorization);
        if (!result.isSuccess()) {
            return Result.error(result.getErrorResult().getCode(), result.getErrorResult().getMessage());
        }
        
        Long userId = result.getUserId();
        
        // 查询用户角色
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(com.ai_hub.enums.ErrorCode.USER_NOT_FOUND);
        }
        
        // 删除帖子
        postService.deletePost(postId, userId, user.getRole());
        
        return Result.success("删除成功", null);
    }

    /**
     * 点赞/取消点赞帖子
     *
     * @param postId 帖子ID
     * @param authorization Authorization头（必需）
     * @param request 点赞请求
     * @return 点赞响应
     */
    @PostMapping("/{postId}/like")
    public Result<LikeResponse> toggleLike(
            @PathVariable Long postId,
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody LikeRequest request) {
        log.info("点赞操作，帖子ID: {}, 操作: {}", postId, request.getAction());
        
        // 验证Token并提取用户ID
        TokenValidator.ValidationResult result = tokenValidator.validateAndExtractUserId(authorization);
        if (!result.isSuccess()) {
            return Result.error(result.getErrorResult().getCode(), result.getErrorResult().getMessage());
        }
        
        Long userId = result.getUserId();
        
        // 执行点赞/取消点赞
        LikeResponse likeResponse = postService.toggleLike(postId, userId, request);
        
        String message = "like".equals(request.getAction()) ? "点赞成功" : "取消点赞成功";
        return Result.success(message, likeResponse);
    }

    /**
     * 收藏/取消收藏帖子
     *
     * @param postId 帖子ID
     * @param authorization Authorization头（必需）
     * @param request 收藏请求
     * @return 收藏响应
     */
    @PostMapping("/{postId}/collect")
    public Result<CollectResponse> toggleCollect(
            @PathVariable Long postId,
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody LikeRequest request) {
        log.info("收藏操作，帖子ID: {}, 操作: {}", postId, request.getAction());
        
        // 验证Token并提取用户ID
        TokenValidator.ValidationResult result = tokenValidator.validateAndExtractUserId(authorization);
        if (!result.isSuccess()) {
            return Result.error(result.getErrorResult().getCode(), result.getErrorResult().getMessage());
        }
        
        Long userId = result.getUserId();
        
        // 执行收藏/取消收藏
        CollectResponse collectResponse = postService.toggleCollect(postId, userId, request);
        
        String message = "collect".equals(request.getAction()) ? "收藏成功" : "取消收藏成功";
        return Result.success(message, collectResponse);
    }

    /**
     * 获取用户发布的帖子列表
     *
     * @param userId 用户ID
     * @param page 页码，默认1
     * @param size 每页条数，默认10
     * @return 分页结果
     */
    @GetMapping("/user/{userId}")
    public Result<PageResult<PostListItemResponse>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("获取用户帖子列表，用户ID: {}, 页码: {}, 每页条数: {}", userId, page, size);
        
        PageResult<PostListItemResponse> result = postService.getUserPosts(userId, page, size);
        
        return Result.success(result);
    }

    /**
     * 获取用户点赞的帖子列表
     *
     * @param userId 用户ID
     * @param page 页码，默认1
     * @param size 每页条数，默认10
     * @return 分页结果
     */
    @GetMapping("/user/{userId}/liked")
    public Result<PageResult<PostListItemResponse>> getUserLikedPosts(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("获取用户点赞的帖子列表，用户ID: {}, 页码: {}, 每页条数: {}", userId, page, size);
        
        PageResult<PostListItemResponse> result = postService.getUserLikedPosts(userId, page, size);
        
        return Result.success(result);
    }

    /**
     * 获取用户收藏的帖子列表
     *
     * @param userId 用户ID
     * @param page 页码，默认1
     * @param size 每页条数，默认10
     * @return 分页结果
     */
    @GetMapping("/user/{userId}/collected")
    public Result<PageResult<PostListItemResponse>> getUserCollectedPosts(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("获取用户收藏的帖子列表，用户ID: {}, 页码: {}, 每页条数: {}", userId, page, size);
        
        PageResult<PostListItemResponse> result = postService.getUserCollectedPosts(userId, page, size);
        
        return Result.success(result);
    }
    
    /**
     * 获取所有帖子标签
     *
     * @return 标签列表
     */
    @GetMapping("/tags")
    public Result<List<String>> getAllTags() {
        log.info("获取所有帖子标签");
        
        List<String> tags = postService.getAllTags();
        
        return Result.success(tags);
    }
}
