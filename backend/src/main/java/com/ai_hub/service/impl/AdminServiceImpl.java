package com.ai_hub.service.impl;

import com.ai_hub.dto.request.AdminPostListRequest;
import com.ai_hub.dto.request.AdminUserListRequest;
import com.ai_hub.dto.response.AdminPostResponse;
import com.ai_hub.dto.response.AdminUserResponse;
import com.ai_hub.dto.response.PageResult;
import com.ai_hub.entity.Comment;
import com.ai_hub.entity.Post;
import com.ai_hub.entity.User;
import com.ai_hub.mapper.CommentMapper;
import com.ai_hub.mapper.PostMapper;
import com.ai_hub.mapper.UserMapper;
import com.ai_hub.service.AdminService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;

    /**
     * 分页查询用户列表
     *
     * @param request 查询请求
     * @return 分页结果
     */
    @Override
    public PageResult<AdminUserResponse> getUserList(AdminUserListRequest request) {
        log.info("管理员查询用户列表，页码: {}, 每页条数: {}, 用户名: {}, 角色: {}, 状态: {}",
                request.getPage(), request.getSize(), request.getUsername(), request.getRole(), request.getStatus());

        // 1. 构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        // 用户名模糊搜索
        if (StringUtils.hasText(request.getUsername())) {
            queryWrapper.like(User::getUsername, request.getUsername());
        }
        
        // 角色筛选
        if (StringUtils.hasText(request.getRole())) {
            queryWrapper.eq(User::getRole, request.getRole());
        }
        
        // 状态筛选
        if (request.getStatus() != null) {
            queryWrapper.eq(User::getStatus, request.getStatus());
        }
        
        // 按创建时间倒序
        queryWrapper.orderByDesc(User::getCreateTime);

        // 2. 分页查询
        Page<User> page = new Page<>(request.getPage(), request.getSize());
        Page<User> userPage = userMapper.selectPage(page, queryWrapper);

        // 3. 转换为响应 DTO（排除密码）
        List<AdminUserResponse> records = userPage.getRecords().stream()
                .map(this::convertToAdminUserResponse)
                .collect(Collectors.toList());

        // 4. 构建分页结果
        return PageResult.of(
                records,
                userPage.getTotal(),
                userPage.getSize(),
                userPage.getCurrent()
        );
    }

    /**
     * 将 User 实体转换为 AdminUserResponse（不包含密码）
     *
     * @param user 用户实体
     * @return 管理员用户响应 DTO
     */
    private AdminUserResponse convertToAdminUserResponse(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .status(user.getStatus())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .build();
    }

    /**
     * 封禁/解封用户
     *
     * @param userId 用户ID
     * @param status 状态 (0:封禁, 1:正常)
     */
    @Override
    public void updateUserStatus(Long userId, Integer status) {
        log.info("更新用户状态，用户ID: {}, 新状态: {}", userId, status);

        // 1. 验证状态值
        if (status != 0 && status != 1) {
            throw new RuntimeException("状态值无效，必须为 0 或 1");
        }

        // 2. 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 3. 如果状态没有变化，直接返回
        if (user.getStatus().equals(status)) {
            log.info("用户状态未变化，无需更新");
            return;
        }

        // 4. 更新状态
        user.setStatus(status);
        userMapper.updateById(user);

        String action = status == 0 ? "封禁" : "解封";
        log.info("用户{}成功，用户ID: {}", action, userId);
    }

    /**
     * 分页查询所有帖子（含待审核）
     *
     * @param request 查询请求
     * @return 分页结果
     */
    @Override
    public PageResult<AdminPostResponse> getPostList(AdminPostListRequest request) {
        log.info("管理员查询帖子列表，页码: {}, 每页条数: {}, 状态: {}, 用户ID: {}",
                request.getPage(), request.getSize(), request.getStatus(), request.getUserId());

        // 1. 构建查询条件
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        
        // 注意：管理员需要查看所有帖子，包括已删除的，所以不使用 @TableLogic
        // 如果需要查看逻辑删除的记录，需要使用自定义 SQL 或者关闭逻辑删除
        
        // 状态筛选
        if (request.getStatus() != null) {
            queryWrapper.eq(Post::getStatus, request.getStatus());
        }
        
        // 用户ID筛选
        if (request.getUserId() != null) {
            queryWrapper.eq(Post::getUserId, request.getUserId());
        }
        
        // 按创建时间倒序
        queryWrapper.orderByDesc(Post::getCreateTime);

        // 2. 分页查询
        Page<Post> page = new Page<>(request.getPage(), request.getSize());
        Page<Post> postPage = postMapper.selectPage(page, queryWrapper);

        // 3. 转换为响应 DTO
        List<AdminPostResponse> records = postPage.getRecords().stream()
                .map(this::convertToAdminPostResponse)
                .collect(Collectors.toList());

        // 4. 构建分页结果
        return PageResult.of(
                records,
                postPage.getTotal(),
                postPage.getSize(),
                postPage.getCurrent()
        );
    }

    /**
     * 将 Post 实体转换为 AdminPostResponse
     *
     * @param post 帖子实体
     * @return 管理员帖子响应 DTO
     */
    private AdminPostResponse convertToAdminPostResponse(Post post) {
        // 生成内容摘要（前100个字符）
        String contentSummary = null;
        if (post.getContent() != null) {
            contentSummary = post.getContent().length() > 100 
                    ? post.getContent().substring(0, 100) + "..." 
                    : post.getContent();
        }

        return AdminPostResponse.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .title(post.getTitle())
                .contentSummary(contentSummary)
                .category(post.getCategory())
                .tags(post.getTags())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .collectCount(post.getCollectCount())
                .commentCount(post.getCommentCount())
                .isSticky(post.getIsSticky())
                .isEssence(post.getIsEssence())
                .status(post.getStatus())
                .createTime(post.getCreateTime())
                .updateTime(post.getUpdateTime())
                .deleted(post.getDeleted())
                .build();
    }

    /**
     * 审核帖子（通过/驳回/删除）
     *
     * @param postId 帖子ID
     * @param status 审核状态 (1:通过, 0:驳回, 2:删除)
     * @param reason 审核原因（可选）
     */
    @Override
    public void auditPost(Long postId, Integer status, String reason) {
        log.info("审核帖子，帖子ID: {}, 新状态: {}, 原因: {}", postId, status, reason);

        // 1. 验证状态值
        if (status != 0 && status != 1 && status != 2) {
            throw new RuntimeException("状态值无效，必须为 0、1 或 2");
        }

        // 2. 查询帖子
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }

        // 3. 如果状态没有变化，直接返回
        if (post.getStatus().equals(status)) {
            log.info("帖子状态未变化，无需更新");
            return;
        }

        // 4. 更新状态
        post.setStatus(status);
        postMapper.updateById(post);

        String action;
        switch (status) {
            case 0:
                action = "驳回";
                break;
            case 1:
                action = "通过";
                break;
            case 2:
                action = "删除";
                break;
            default:
                action = "未知操作";
        }

        log.info("帖子{}成功，帖子ID: {}, 原因: {}", action, postId, reason);
    }

    /**
     * 置顶/取消置顶帖子
     *
     * @param postId 帖子ID
     * @param sticky 是否置顶 (true:置顶, false:取消置顶)
     */
    @Override
    public void updatePostSticky(Long postId, Boolean sticky) {
        log.info("更新帖子置顶状态，帖子ID: {}, 新状态: {}", postId, sticky);

        // 1. 查询帖子
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }

        // 2. 转换为数据库存储的值 (true -> 1, false -> 0)
        Integer stickyValue = sticky ? 1 : 0;

        // 3. 如果状态没有变化，直接返回
        if (post.getIsSticky().equals(stickyValue)) {
            log.info("帖子置顶状态未变化，无需更新");
            return;
        }

        // 4. 更新状态
        post.setIsSticky(stickyValue);
        postMapper.updateById(post);

        String action = sticky ? "置顶" : "取消置顶";
        log.info("帖子{}成功，帖子ID: {}", action, postId);
    }

    /**
     * 加精/取消加精帖子
     *
     * @param postId 帖子ID
     * @param essence 是否加精 (true:加精, false:取消加精)
     */
    @Override
    public void updatePostEssence(Long postId, Boolean essence) {
        log.info("更新帖子加精状态，帖子ID: {}, 新状态: {}", postId, essence);

        // 1. 查询帖子
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }

        // 2. 转换为数据库存储的值 (true -> 1, false -> 0)
        Integer essenceValue = essence ? 1 : 0;

        // 3. 如果状态没有变化，直接返回
        if (post.getIsEssence().equals(essenceValue)) {
            log.info("帖子加精状态未变化，无需更新");
            return;
        }

        // 4. 更新状态
        post.setIsEssence(essenceValue);
        postMapper.updateById(post);

        String action = essence ? "加精" : "取消加精";
        log.info("帖子{}成功，帖子ID: {}", action, postId);
    }

    /**
     * 删除任何评论
     *
     * @param commentId 评论ID
     */
    @Override
    public void deleteComment(Long commentId) {
        log.info("管理员删除评论，评论ID: {}", commentId);

        // 1. 查询评论
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        // 2. 执行逻辑删除（MyBatis-Plus 的 @TableLogic 会自动处理）
        commentMapper.deleteById(commentId);

        log.info("评论删除成功，评论ID: {}", commentId);
    }
}
