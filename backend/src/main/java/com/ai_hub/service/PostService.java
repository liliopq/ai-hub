package com.ai_hub.service;

import com.ai_hub.dto.request.CreatePostRequest;
import com.ai_hub.dto.request.LikeRequest;
import com.ai_hub.dto.request.PostListRequest;
import com.ai_hub.dto.request.UpdatePostRequest;
import com.ai_hub.dto.response.*;

import java.util.List;

/**
 * 帖子服务接口
 */
public interface PostService {

    /**
     * 发布帖子
     *
     * @param userId 用户ID
     * @param request 发布帖子请求
     * @return 帖子创建响应
     */
    CreatePostResponse createPost(Long userId, CreatePostRequest request);

    /**
     * 分页获取帖子列表
     *
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<PostListItemResponse> getPostList(PostListRequest request);

    /**
     * 获取帖子详情
     *
     * @param postId 帖子ID
     * @param currentUserId 当前用户ID（可选，用于判断是否点赞和收藏）
     * @return 帖子详情
     */
    PostDetailResponse getPostDetail(Long postId, Long currentUserId);

    /**
     * 更新帖子
     *
     * @param postId 帖子ID
     * @param userId 当前用户ID
     * @param userRole 当前用户角色
     * @param request 更新请求
     * @return 更新后的帖子详情
     */
    PostDetailResponse updatePost(Long postId, Long userId, String userRole, UpdatePostRequest request);

    /**
     * 删除帖子（软删除）
     *
     * @param postId 帖子ID
     * @param userId 当前用户ID
     * @param userRole 当前用户角色
     */
    void deletePost(Long postId, Long userId, String userRole);

    /**
     * 点赞/取消点赞帖子
     *
     * @param postId 帖子ID
     * @param userId 当前用户ID
     * @param request 点赞请求
     * @return 点赞响应
     */
    LikeResponse toggleLike(Long postId, Long userId, LikeRequest request);

    /**
     * 收藏/取消收藏帖子
     *
     * @param postId 帖子ID
     * @param userId 当前用户ID
     * @param request 收藏请求
     * @return 收藏响应
     */
    CollectResponse toggleCollect(Long postId, Long userId, LikeRequest request);

    /**
     * 获取用户发布的帖子列表
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页条数
     * @return 分页结果
     */
    PageResult<PostListItemResponse> getUserPosts(Long userId, Integer page, Integer size);

    /**
     * 获取用户点赞的帖子列表
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页条数
     * @return 分页结果
     */
    PageResult<PostListItemResponse> getUserLikedPosts(Long userId, Integer page, Integer size);

    /**
     * 获取用户收藏的帖子列表
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页条数
     * @return 分页结果
     */
    PageResult<PostListItemResponse> getUserCollectedPosts(Long userId, Integer page, Integer size);
    
    /**
     * 获取所有帖子标签
     *
     * @return 标签列表
     */
    List<String> getAllTags();
}
