package com.ai_hub.service.impl;

import com.ai_hub.dto.request.CreatePostRequest;
import com.ai_hub.dto.request.LikeRequest;
import com.ai_hub.dto.request.PostListRequest;
import com.ai_hub.dto.request.UpdatePostRequest;
import com.ai_hub.dto.response.*;
import com.ai_hub.entity.Post;
import com.ai_hub.entity.PostCollect;
import com.ai_hub.entity.PostLike;
import com.ai_hub.entity.User;
import com.ai_hub.enums.ErrorCode;
import com.ai_hub.mapper.PostCollectMapper;
import com.ai_hub.mapper.PostLikeMapper;
import com.ai_hub.mapper.PostMapper;
import com.ai_hub.mapper.UserMapper;
import com.ai_hub.service.PostService;
import com.ai_hub.service.WebSocketNotificationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 帖子服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final PostLikeMapper postLikeMapper;
    private final PostCollectMapper postCollectMapper;
    private final WebSocketNotificationService webSocketNotificationService;

    /**
     * 发布帖子
     *
     * @param userId 用户ID
     * @param request 发布帖子请求
     * @return 帖子创建响应
     */
    @Override
    public CreatePostResponse createPost(Long userId, CreatePostRequest request) {
        log.info("发布帖子，用户ID: {}, 标题: {}", userId, request.getTitle());

        // 1. 创建帖子实体
        Post post = new Post();
        post.setUserId(userId);
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setCategory(request.getCategory());
        
        // 将标签数组转换为字符串存储
        if (request.getTags() != null && request.getTags().length > 0) {
            post.setTags(String.join(",", request.getTags()));
        }
        
        // 设置默认值
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setIsSticky(0);  // 默认不置顶
        post.setIsEssence(0); // 默认非精华
        post.setStatus(1);    // 默认状态为正常

        // 2. 保存帖子（createTime 和 updateTime 会自动填充）
        postMapper.insert(post);

        log.info("帖子发布成功，帖子ID: {}", post.getId());

        // 3. 构建简化响应（只包含id、title、createTime）
        CreatePostResponse response = new CreatePostResponse();
        response.setId(post.getId());
        response.setTitle(post.getTitle());
        response.setCreateTime(post.getCreateTime());

        return response;
    }

    /**
     * 分页获取帖子列表
     *
     * @param request 查询请求
     * @return 分页结果
     */
    @Override
    public PageResult<PostListItemResponse> getPostList(PostListRequest request) {
        log.info("获取帖子列表，页码: {}, 每页条数: {}, 分类: {}, 排序: {}, 标签: {}, 关键词: {}",
                request.getPage(), request.getSize(), request.getCategory(), request.getSortBy(),
                request.getTag(), request.getKeyword());

        // 1. 构建查询条件
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        
        // 只查询正常状态的帖子
        queryWrapper.eq(Post::getStatus, 1);
        
        // 分类筛选
        if (StringUtils.hasText(request.getCategory())) {
            queryWrapper.eq(Post::getCategory, request.getCategory());
        }
        
        // 标签筛选（标签以逗号分隔存储，使用 LIKE 查询）
        if (StringUtils.hasText(request.getTag())) {
            queryWrapper.like(Post::getTags, request.getTag());
        }
        
        // 搜索关键词（搜索标题和内容）
        if (StringUtils.hasText(request.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Post::getTitle, request.getKeyword())
                    .or()
                    .like(Post::getContent, request.getKeyword())
            );
        }
        
        // 排序
        if ("hot".equals(request.getSortBy())) {
            // 按热度排序（点赞数 + 浏览数）
            queryWrapper.orderByDesc(Post::getIsSticky)  // 置顶优先
                    .orderByDesc(Post::getLikeCount, Post::getViewCount);
        } else {
            // 默认按时间排序（置顶优先）
            queryWrapper.orderByDesc(Post::getIsSticky)
                    .orderByDesc(Post::getCreateTime);
        }

        // 2. 分页查询
        Page<Post> page = new Page<>(request.getPage(), request.getSize());
        Page<Post> postPage = postMapper.selectPage(page, queryWrapper);

        // 3. 收集所有用户ID，批量查询用户信息
        List<Long> userIds = postPage.getRecords().stream()
                .map(Post::getUserId)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量查询用户信息
        List<User> users = userIds.isEmpty() ? List.of() : userMapper.selectBatchIds(userIds);
        
        // 构建用户ID到用户对象的映射
        java.util.Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 4. 转换为响应DTO
        List<PostListItemResponse> records = postPage.getRecords().stream()
                .map(post -> convertToPostListItemResponse(post, userMap))
                .collect(Collectors.toList());

        // 5. 构建分页结果
        return PageResult.of(
                records,
                postPage.getTotal(),
                postPage.getSize(),
                postPage.getCurrent()
        );
    }

    /**
     * 将Post实体转换为PostListItemResponse
     *
     * @param post 帖子实体
     * @param userMap 用户ID到用户对象的映射
     * @return 帖子列表项响应
     */
    private PostListItemResponse convertToPostListItemResponse(Post post, java.util.Map<Long, User> userMap) {
        PostListItemResponse response = new PostListItemResponse();
        response.setId(post.getId());
        response.setTitle(post.getTitle());
        response.setCategory(post.getCategory());
        response.setViewCount(post.getViewCount());
        response.setLikeCount(post.getLikeCount());
        response.setCommentCount(post.getCommentCount());
        response.setCreateTime(post.getCreateTime());

        // 转换标签字符串为列表
        if (StringUtils.hasText(post.getTags())) {
            response.setTags(Arrays.asList(post.getTags().split(",")));
        } else {
            response.setTags(List.of());
        }

        // 从映射中获取用户信息
        User user = userMap.get(post.getUserId());
        if (user != null) {
            PostListItemResponse.UserBasicInfo userInfo = new PostListItemResponse.UserBasicInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setAvatar(user.getAvatar());
            response.setUser(userInfo);
        }

        return response;
    }

    /**
     * 获取帖子详情
     *
     * @param postId 帖子ID
     * @param currentUserId 当前用户ID（可选，用于判断是否点赞和收藏）
     * @return 帖子详情
     */
    @Override
    public PostDetailResponse getPostDetail(Long postId, Long currentUserId) {
        log.info("获取帖子详情，帖子ID: {}, 当前用户ID: {}", postId, currentUserId);

        // 1. 查询帖子
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException(ErrorCode.POST_NOT_FOUND.getMessage());
        }

        // 2. 检查帖子状态是否正常
        if (post.getStatus() != 1) {
            throw new RuntimeException(ErrorCode.POST_NOT_FOUND.getMessage());
        }

        // 3. 增加浏览次数
        post.setViewCount(post.getViewCount() + 1);
        postMapper.updateById(post);

        // 4. 构建响应
        PostDetailResponse response = new PostDetailResponse();
        response.setId(post.getId());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setCategory(post.getCategory());
        response.setViewCount(post.getViewCount());
        response.setLikeCount(post.getLikeCount());
        response.setCommentCount(post.getCommentCount());
        response.setCreateTime(post.getCreateTime());
        response.setUpdateTime(post.getUpdateTime());

        // 5. 转换标签字符串为列表
        if (StringUtils.hasText(post.getTags())) {
            response.setTags(Arrays.asList(post.getTags().split(",")));
        } else {
            response.setTags(List.of());
        }

        // 6. 查询用户信息
        User user = userMapper.selectById(post.getUserId());
        if (user != null) {
            PostDetailResponse.UserBasicInfo userInfo = new PostDetailResponse.UserBasicInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setAvatar(user.getAvatar());
            response.setUser(userInfo);
        }

        // 7. 判断当前用户是否点赞
        if (currentUserId != null) {
            LambdaQueryWrapper<PostLike> likeQueryWrapper = new LambdaQueryWrapper<>();
            likeQueryWrapper.eq(PostLike::getPostId, postId)
                    .eq(PostLike::getUserId, currentUserId);
            Long likeCount = postLikeMapper.selectCount(likeQueryWrapper);
            response.setIsLiked(likeCount > 0);
            
            // 8. 判断当前用户是否收藏
            LambdaQueryWrapper<PostCollect> collectQueryWrapper = new LambdaQueryWrapper<>();
            collectQueryWrapper.eq(PostCollect::getPostId, postId)
                    .eq(PostCollect::getUserId, currentUserId);
            Long collectCount = postCollectMapper.selectCount(collectQueryWrapper);
            response.setIsCollected(collectCount > 0);
        } else {
            response.setIsLiked(false);
            response.setIsCollected(false);
        }

        return response;
    }

    /**
     * 更新帖子
     *
     * @param postId 帖子ID
     * @param userId 当前用户ID
     * @param userRole 当前用户角色
     * @param request 更新请求
     * @return 更新后的帖子详情
     */
    @Override
    public PostDetailResponse updatePost(Long postId, Long userId, String userRole, UpdatePostRequest request) {
        log.info("更新帖子，帖子ID: {}, 用户ID: {}, 角色: {}", postId, userId, userRole);

        // 1. 查询帖子
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException(ErrorCode.POST_NOT_FOUND.getMessage());
        }

        // 2. 权限验证：只有帖主或管理员可以更新
        if (!post.getUserId().equals(userId) && !"ADMIN".equals(userRole)) {
            throw new RuntimeException("没有权限更新该帖子");
        }

        // 3. 部分更新（只更新非空字段）
        if (StringUtils.hasText(request.getTitle())) {
            post.setTitle(request.getTitle());
        }

        if (StringUtils.hasText(request.getContent())) {
            post.setContent(request.getContent());
        }

        if (StringUtils.hasText(request.getCategory())) {
            post.setCategory(request.getCategory());
        }

        if (request.getTags() != null && request.getTags().length > 0) {
            post.setTags(String.join(",", request.getTags()));
        }

        // 4. 保存更新（updateTime 会自动填充）
        postMapper.updateById(post);

        log.info("帖子更新成功，帖子ID: {}", postId);

        // 5. 返回更新后的帖子详情
        return getPostDetail(postId, userId);
    }

    /**
     * 删除帖子（软删除）
     *
     * @param postId 帖子ID
     * @param userId 当前用户ID
     * @param userRole 当前用户角色
     */
    @Override
    public void deletePost(Long postId, Long userId, String userRole) {
        log.info("删除帖子，帖子ID: {}, 用户ID: {}, 角色: {}", postId, userId, userRole);

        // 1. 查询帖子
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException(ErrorCode.POST_NOT_FOUND.getMessage());
        }

        // 2. 权限验证：只有帖主或管理员可以删除
        if (!post.getUserId().equals(userId) && !"ADMIN".equals(userRole)) {
            throw new RuntimeException("没有权限删除该帖子");
        }

        // 3. 软删除（MyBatis-Plus 的 @TableLogic 会自动处理）
        postMapper.deleteById(postId);

        log.info("帖子删除成功，帖子ID: {}", postId);
    }

    /**
     * 点赞/取消点赞帖子
     *
     * @param postId 帖子ID
     * @param userId 当前用户ID
     * @param request 点赞请求
     * @return 点赞响应
     */
    @Override
    public LikeResponse toggleLike(Long postId, Long userId, LikeRequest request) {
        log.info("点赞操作，帖子ID: {}, 用户ID: {}, 操作: {}", postId, userId, request.getAction());

        // 1. 查询帖子
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException(ErrorCode.POST_NOT_FOUND.getMessage());
        }

        // 2. 检查帖子状态是否正常
        if (post.getStatus() != 1) {
            throw new RuntimeException(ErrorCode.POST_NOT_FOUND.getMessage());
        }

        // 3. 根据操作类型执行点赞或取消点赞
        if ("like".equals(request.getAction())) {
            // 点赞
            return likePost(postId, userId, post);
        } else if ("unlike".equals(request.getAction())) {
            // 取消点赞
            return unlikePost(postId, userId, post);
        } else {
            throw new RuntimeException("无效的操作类型");
        }
    }

    /**
     * 点赞帖子
     */
    private LikeResponse likePost(Long postId, Long userId, Post post) {
        // 检查是否已经点赞过
        LambdaQueryWrapper<PostLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PostLike::getPostId, postId)
                .eq(PostLike::getUserId, userId);
        Long count = postLikeMapper.selectCount(queryWrapper);

        if (count > 0) {
            throw new RuntimeException(ErrorCode.POST_ALREADY_LIKED.getMessage());
        }

        // 创建点赞记录
        PostLike postLike = new PostLike();
        postLike.setPostId(postId);
        postLike.setUserId(userId);
        postLikeMapper.insert(postLike);

        // 更新帖子点赞数
        post.setLikeCount(post.getLikeCount() + 1);
        postMapper.updateById(post);

        log.info("点赞成功，帖子ID: {}, 用户ID: {}", postId, userId);

        // 发送点赞通知给帖子作者（排除自己点赞自己的情况）
        if (!userId.equals(post.getUserId())) {
            webSocketNotificationService.sendLikeNotification(post.getUserId(), userId, postId);
        }

        return new LikeResponse(post.getLikeCount(), true);
    }

    /**
     * 取消点赞帖子
     */
    private LikeResponse unlikePost(Long postId, Long userId, Post post) {
        // 检查是否点赞过
        LambdaQueryWrapper<PostLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PostLike::getPostId, postId)
                .eq(PostLike::getUserId, userId);
        PostLike postLike = postLikeMapper.selectOne(queryWrapper);

        if (postLike == null) {
            throw new RuntimeException(ErrorCode.POST_NOT_LIKED.getMessage());
        }

        // 删除点赞记录
        postLikeMapper.deleteById(postLike.getId());

        // 更新帖子点赞数
        post.setLikeCount(post.getLikeCount() - 1);
        postMapper.updateById(post);

        log.info("取消点赞成功，帖子ID: {}, 用户ID: {}", postId, userId);

        return new LikeResponse(post.getLikeCount(), false);
    }

    /**
     * 收藏/取消收藏帖子
     *
     * @param postId 帖子ID
     * @param userId 当前用户ID
     * @param request 收藏请求
     * @return 收藏响应
     */
    @Override
    public CollectResponse toggleCollect(Long postId, Long userId, LikeRequest request) {
        log.info("收藏操作，帖子ID: {}, 用户ID: {}, 操作: {}", postId, userId, request.getAction());

        // 1. 查询帖子
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException(ErrorCode.POST_NOT_FOUND.getMessage());
        }

        // 2. 检查帖子状态是否正常
        if (post.getStatus() != 1) {
            throw new RuntimeException(ErrorCode.POST_NOT_FOUND.getMessage());
        }

        // 3. 根据操作类型执行收藏或取消收藏
        if ("collect".equals(request.getAction())) {
            // 收藏
            return collectPost(postId, userId, post);
        } else if ("uncollect".equals(request.getAction())) {
            // 取消收藏
            return uncollectPost(postId, userId, post);
        } else {
            throw new RuntimeException("无效的操作类型");
        }
    }

    /**
     * 收藏帖子
     */
    private CollectResponse collectPost(Long postId, Long userId, Post post) {
        // 检查是否已经收藏过
        LambdaQueryWrapper<PostCollect> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PostCollect::getPostId, postId)
                .eq(PostCollect::getUserId, userId);
        Long count = postCollectMapper.selectCount(queryWrapper);

        if (count > 0) {
            throw new RuntimeException("已经收藏过了");
        }

        // 创建收藏记录
        PostCollect postCollect = new PostCollect();
        postCollect.setPostId(postId);
        postCollect.setUserId(userId);
        postCollectMapper.insert(postCollect);

        // 初始化收藏数（如果为null）
        if (post.getCollectCount() == null) {
            post.setCollectCount(0);
        }
        
        // 更新帖子收藏数
        post.setCollectCount(post.getCollectCount() + 1);
        postMapper.updateById(post);

        log.info("收藏成功，帖子ID: {}, 用户ID: {}", postId, userId);

        // 发送收藏通知给帖子作者（排除自己收藏自己的情况）
        if (!userId.equals(post.getUserId())) {
            webSocketNotificationService.sendCollectNotification(post.getUserId(), userId, postId);
        }

        return new CollectResponse(post.getCollectCount(), true);
    }

    /**
     * 取消收藏帖子
     */
    private CollectResponse uncollectPost(Long postId, Long userId, Post post) {
        // 检查是否收藏过
        LambdaQueryWrapper<PostCollect> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PostCollect::getPostId, postId)
                .eq(PostCollect::getUserId, userId);
        PostCollect postCollect = postCollectMapper.selectOne(queryWrapper);

        if (postCollect == null) {
            throw new RuntimeException("还未收藏");
        }

        // 删除收藏记录
        postCollectMapper.deleteById(postCollect.getId());

        // 初始化收藏数（如果为null）
        if (post.getCollectCount() == null) {
            post.setCollectCount(0);
        }
        
        // 更新帖子收藏数
        post.setCollectCount(post.getCollectCount() - 1);
        postMapper.updateById(post);

        log.info("取消收藏成功，帖子ID: {}, 用户ID: {}", postId, userId);

        return new CollectResponse(post.getCollectCount(), false);
    }

    /**
     * 获取用户发布的帖子列表
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页条数
     * @return 分页结果
     */
    @Override
    public PageResult<PostListItemResponse> getUserPosts(Long userId, Integer page, Integer size) {
        log.info("获取用户帖子列表，用户ID: {}, 页码: {}, 每页条数: {}", userId, page, size);

        // 1. 构建查询条件
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Post::getUserId, userId)
                .eq(Post::getStatus, 1)
                .orderByDesc(Post::getCreateTime);

        // 2. 分页查询
        Page<Post> postPage = postMapper.selectPage(new Page<>(page, size), queryWrapper);

        // 3. 收集用户信息
        User user = userMapper.selectById(userId);
        java.util.Map<Long, User> userMap = new java.util.HashMap<>();
        if (user != null) {
            userMap.put(userId, user);
        }

        // 4. 转换为响应DTO
        List<PostListItemResponse> records = postPage.getRecords().stream()
                .map(post -> convertToPostListItemResponse(post, userMap))
                .collect(Collectors.toList());

        // 5. 构建分页结果
        return PageResult.of(
                records,
                postPage.getTotal(),
                postPage.getSize(),
                postPage.getCurrent()
        );
    }

    /**
     * 获取用户点赞的帖子列表
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页条数
     * @return 分页结果
     */
    @Override
    public PageResult<PostListItemResponse> getUserLikedPosts(Long userId, Integer page, Integer size) {
        log.info("获取用户点赞的帖子列表，用户ID: {}, 页码: {}, 每页条数: {}", userId, page, size);

        // 1. 查询用户点赞的帖子ID列表
        LambdaQueryWrapper<PostLike> likeQueryWrapper = new LambdaQueryWrapper<>();
        likeQueryWrapper.eq(PostLike::getUserId, userId)
                .orderByDesc(PostLike::getCreateTime);
        List<PostLike> postLikes = postLikeMapper.selectPage(new Page<>(page, size), likeQueryWrapper).getRecords();
        
        if (postLikes.isEmpty()) {
            return PageResult.of(List.of(), 0L, size.longValue(), page.longValue());
        }
        
        List<Long> postIds = postLikes.stream()
                .map(PostLike::getPostId)
                .collect(Collectors.toList());

        // 2. 根据帖子ID查询帖子详情
        LambdaQueryWrapper<Post> postQueryWrapper = new LambdaQueryWrapper<>();
        postQueryWrapper.in(Post::getId, postIds)
                .eq(Post::getStatus, 1)
                .orderByDesc(Post::getCreateTime);
        
        Page<Post> postPage = postMapper.selectPage(new Page<>(1, postIds.size()), postQueryWrapper);

        // 3. 收集用户信息
        java.util.Map<Long, User> userMap = new java.util.HashMap<>();
        for (Post post : postPage.getRecords()) {
            if (!userMap.containsKey(post.getUserId())) {
                User user = userMapper.selectById(post.getUserId());
                if (user != null) {
                    userMap.put(post.getUserId(), user);
                }
            }
        }

        // 4. 转换为响应DTO
        List<PostListItemResponse> records = postPage.getRecords().stream()
                .map(post -> convertToPostListItemResponse(post, userMap))
                .collect(Collectors.toList());

        // 5. 构建分页结果
        return PageResult.of(
                records,
                (long) postIds.size(),
                size.longValue(),
                page.longValue()
        );
    }

    /**
     * 获取用户收藏的帖子列表
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页条数
     * @return 分页结果
     */
    @Override
    public PageResult<PostListItemResponse> getUserCollectedPosts(Long userId, Integer page, Integer size) {
        log.info("获取用户收藏的帖子列表，用户ID: {}, 页码: {}, 每页条数: {}", userId, page, size);

        // 1. 查询用户收藏的帖子ID列表
        LambdaQueryWrapper<PostCollect> collectQueryWrapper = new LambdaQueryWrapper<>();
        collectQueryWrapper.eq(PostCollect::getUserId, userId)
                .orderByDesc(PostCollect::getCreateTime);
        List<PostCollect> postCollects = postCollectMapper.selectPage(new Page<>(page, size), collectQueryWrapper).getRecords();
        
        if (postCollects.isEmpty()) {
            return PageResult.of(List.of(), 0L, size.longValue(), page.longValue());
        }
        
        List<Long> postIds = postCollects.stream()
                .map(PostCollect::getPostId)
                .collect(Collectors.toList());

        // 2. 根据帖子ID查询帖子详情
        LambdaQueryWrapper<Post> postQueryWrapper = new LambdaQueryWrapper<>();
        postQueryWrapper.in(Post::getId, postIds)
                .eq(Post::getStatus, 1)
                .orderByDesc(Post::getCreateTime);
        
        Page<Post> postPage = postMapper.selectPage(new Page<>(1, postIds.size()), postQueryWrapper);

        // 3. 收集用户信息
        java.util.Map<Long, User> userMap = new java.util.HashMap<>();
        for (Post post : postPage.getRecords()) {
            if (!userMap.containsKey(post.getUserId())) {
                User user = userMapper.selectById(post.getUserId());
                if (user != null) {
                    userMap.put(post.getUserId(), user);
                }
            }
        }

        // 4. 转换为响应DTO
        List<PostListItemResponse> records = postPage.getRecords().stream()
                .map(post -> convertToPostListItemResponse(post, userMap))
                .collect(Collectors.toList());

        // 5. 构建分页结果
        return PageResult.of(
                records,
                (long) postIds.size(),
                size.longValue(),
                page.longValue()
        );
    }
    
    /**
     * 获取所有帖子标签
     */
    @Override
    public List<String> getAllTags() {
        log.info("获取所有帖子标签");
        
        // 1. 查询所有正常状态的帖子的标签
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Post::getStatus, 1)
                .select(Post::getTags);
        
        List<Post> posts = postMapper.selectList(queryWrapper);
        
        // 2. 提取所有标签并去重
        return posts.stream()
                .filter(post -> post.getTags() != null && !post.getTags().isEmpty())
                .flatMap(post -> Arrays.stream(post.getTags().split(",")))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }
}
