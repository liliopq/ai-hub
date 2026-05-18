package com.ai_hub.service.impl;

import com.ai_hub.dto.request.CommentListRequest;
import com.ai_hub.dto.request.CreateCommentRequest;
import com.ai_hub.dto.response.CommentLikeResponse;
import com.ai_hub.dto.response.CommentResponse;
import com.ai_hub.dto.response.CommentTreeNode;
import com.ai_hub.dto.response.PageResult;
import com.ai_hub.entity.Comment;
import com.ai_hub.entity.CommentLike;
import com.ai_hub.entity.Post;
import com.ai_hub.entity.User;
import com.ai_hub.enums.ErrorCode;
import com.ai_hub.mapper.CommentLikeMapper;
import com.ai_hub.mapper.CommentMapper;
import com.ai_hub.mapper.PostMapper;
import com.ai_hub.mapper.UserMapper;
import com.ai_hub.service.CommentService;
import com.ai_hub.service.WebSocketNotificationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评论服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    
    private final CommentMapper commentMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final WebSocketNotificationService webSocketNotificationService;
    
    /**
     * 发表评论（或回复评论）
     *
     * @param userId 当前用户ID
     * @param request 评论请求
     * @return 评论响应
     */
    @Override
    @Transactional
    public CommentResponse createComment(Long userId, CreateCommentRequest request) {
        log.info("发表评论，用户ID: {}, 帖子ID: {}, 父评论ID: {}", userId, request.getPostId(), request.getParentId());
        
        // 1. 验证帖子是否存在且状态正常
        Post post = postMapper.selectById(request.getPostId());
        if (post == null) {
            throw new RuntimeException(ErrorCode.POST_NOT_FOUND.getMessage());
        }
        if (post.getStatus() != 1) {
            throw new RuntimeException("帖子状态异常，无法评论");
        }
        
        // 2. 如果是回复评论，验证父评论是否存在
        if (request.getParentId() != null && request.getParentId() != 0) {
            Comment parentComment = commentMapper.selectById(request.getParentId());
            if (parentComment == null) {
                throw new RuntimeException("父评论不存在");
            }
            // 验证父评论是否属于该帖子
            if (!parentComment.getPostId().equals(request.getPostId())) {
                throw new RuntimeException("父评论不属于该帖子");
            }
        }
        
        // 3. 验证用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException(ErrorCode.USER_NOT_FOUND.getMessage());
        }
        
        // 4. 创建评论
        Comment comment = new Comment();
        comment.setPostId(request.getPostId());
        comment.setUserId(userId);
        comment.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        comment.setContent(request.getContent());
        comment.setLikeCount(0);
        comment.setStatus(1); // 默认状态为正常
        
        commentMapper.insert(comment);
        
        // 5. 更新帖子的评论数
        post.setCommentCount(post.getCommentCount() != null ? post.getCommentCount() + 1 : 1);
        postMapper.updateById(post);
        
        log.info("评论发表成功，评论ID: {}", comment.getId());

        // 6. 发送评论通知给帖子作者（排除自己评论自己帖子的情况）
        if (!userId.equals(post.getUserId())) {
            webSocketNotificationService.sendCommentNotification(post.getUserId(), userId, request.getPostId(), comment.getId(), request.getContent());
        }
        
        // 7. 构建响应
        return buildCommentResponse(comment, user);
    }
    
    /**
     * 构建评论响应
     */
    private CommentResponse buildCommentResponse(Comment comment, User user) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setCreateTime(comment.getCreateTime());
        
        // 构建用户信息
        CommentResponse.UserBasicInfo userInfo = new CommentResponse.UserBasicInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setAvatar(user.getAvatar());
        response.setUser(userInfo);
        
        return response;
    }
    
    /**
     * 获取帖子的评论树
     *
     * @param postId 帖子ID
     * @param request 分页请求
     * @return 评论树分页结果
     */
    @Override
    public PageResult<CommentTreeNode> getCommentTree(Long postId, CommentListRequest request) {
        log.info("获取帖子评论树，帖子ID: {}, 页码: {}, 每页条数: {}", postId, request.getPage(), request.getSize());
        
        // 1. 验证帖子是否存在
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException(ErrorCode.POST_NOT_FOUND.getMessage());
        }
        
        // 2. 分页查询顶层评论（parentId = 0）
        Page<Comment> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getPostId, postId)
                .eq(Comment::getParentId, 0)
                .eq(Comment::getStatus, 1)
                .orderByDesc(Comment::getCreateTime);
        
        Page<Comment> commentPage = commentMapper.selectPage(page, queryWrapper);
        
        // 3. 获取所有顶层评论的ID
        List<Long> parentIds = commentPage.getRecords().stream()
                .map(Comment::getId)
                .collect(Collectors.toList());
        
        // 4. 查询这些顶层评论的所有子评论
        Map<Long, List<Comment>> repliesMap = null;
        if (!parentIds.isEmpty()) {
            LambdaQueryWrapper<Comment> repliesQuery = new LambdaQueryWrapper<>();
            repliesQuery.in(Comment::getParentId, parentIds)
                    .eq(Comment::getStatus, 1)
                    .orderByAsc(Comment::getCreateTime);
            
            List<Comment> allReplies = commentMapper.selectList(repliesQuery);
            
            // 按 parentId 分组
            repliesMap = allReplies.stream()
                    .collect(Collectors.groupingBy(Comment::getParentId));
        }
        
        // 5. 获取所有相关用户的ID
        List<Long> userIds = new ArrayList<>();
        commentPage.getRecords().forEach(c -> userIds.add(c.getUserId()));
        if (repliesMap != null) {
            repliesMap.values().forEach(list -> list.forEach(c -> userIds.add(c.getUserId())));
        }
        
        // 6. 批量查询用户信息
        Map<Long, User> userMap = null;
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, u -> u));
        }
        
        // 7. 构建评论树
        final Map<Long, List<Comment>> finalRepliesMap = repliesMap;
        final Map<Long, User> finalUserMap = userMap;
        
        List<CommentTreeNode> treeNodes = commentPage.getRecords().stream()
                .map(comment -> buildCommentTreeNode(comment, finalRepliesMap, finalUserMap))
                .collect(Collectors.toList());
        
        // 8. 构建分页结果
        PageResult<CommentTreeNode> result = new PageResult<>();
        result.setRecords(treeNodes);
        result.setTotal(commentPage.getTotal());
        result.setSize(commentPage.getSize());
        result.setCurrent(commentPage.getCurrent());
        result.setPages(commentPage.getPages());
        
        log.info("获取评论树成功，总数: {}", commentPage.getTotal());
        
        return result;
    }
    
    /**
     * 构建评论树节点
     */
    private CommentTreeNode buildCommentTreeNode(Comment comment, 
                                                  Map<Long, List<Comment>> repliesMap,
                                                  Map<Long, User> userMap) {
        CommentTreeNode node = new CommentTreeNode();
        node.setId(comment.getId());
        node.setContent(comment.getContent());
        node.setLikeCount(comment.getLikeCount());
        node.setCreateTime(comment.getCreateTime());
        
        // 设置用户信息
        User user = userMap != null ? userMap.get(comment.getUserId()) : null;
        if (user != null) {
            CommentTreeNode.UserBasicInfo userInfo = new CommentTreeNode.UserBasicInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setAvatar(user.getAvatar());
            node.setUser(userInfo);
        }
        
        // 设置回复列表
        List<Comment> replies = repliesMap != null ? repliesMap.get(comment.getId()) : null;
        if (replies != null && !replies.isEmpty()) {
            List<CommentTreeNode> replyNodes = replies.stream()
                    .map(reply -> buildReplyNode(reply, userMap))
                    .collect(Collectors.toList());
            node.setReplies(replyNodes);
        } else {
            node.setReplies(new ArrayList<>());
        }
        
        return node;
    }
    
    /**
     * 构建回复节点（不包含replies字段，避免递归过深）
     */
    private CommentTreeNode buildReplyNode(Comment comment, Map<Long, User> userMap) {
        CommentTreeNode node = new CommentTreeNode();
        node.setId(comment.getId());
        node.setContent(comment.getContent());
        node.setLikeCount(comment.getLikeCount());
        node.setCreateTime(comment.getCreateTime());
        node.setReplies(new ArrayList<>()); // 回复不再包含子回复
        
        // 设置用户信息
        User user = userMap != null ? userMap.get(comment.getUserId()) : null;
        if (user != null) {
            CommentTreeNode.UserBasicInfo userInfo = new CommentTreeNode.UserBasicInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setAvatar(user.getAvatar());
            node.setUser(userInfo);
        }
        
        return node;
    }
    
    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @param userId 当前用户ID
     * @param userRole 当前用户角色
     */
    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId, String userRole) {
        log.info("删除评论，评论ID: {}, 用户ID: {}, 角色: {}", commentId, userId, userRole);
        
        // 1. 查询评论
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        
        // 2. 权限验证：只有评论作者或管理员可以删除
        if (!comment.getUserId().equals(userId) && !"ADMIN".equals(userRole)) {
            throw new RuntimeException("没有权限删除该评论");
        }
        
        // 3. 软删除评论（MyBatis-Plus 的 @TableLogic 会自动处理）
        commentMapper.deleteById(commentId);
        
        // 4. 更新帖子的评论数（减少1）
        Post post = postMapper.selectById(comment.getPostId());
        if (post != null && post.getCommentCount() != null && post.getCommentCount() > 0) {
            post.setCommentCount(post.getCommentCount() - 1);
            postMapper.updateById(post);
        }
        
        log.info("评论删除成功，评论ID: {}", commentId);
    }
    
    /**
     * 点赞/取消点赞评论
     *
     * @param commentId 评论ID
     * @param userId 当前用户ID
     * @return 点赞响应
     */
    @Override
    @Transactional
    public CommentLikeResponse likeComment(Long commentId, Long userId) {
        log.info("点赞评论，评论ID: {}, 用户ID: {}", commentId, userId);
        
        // 1. 验证评论是否存在
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        
        // 2. 检查是否已经点过赞
        LambdaQueryWrapper<CommentLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommentLike::getCommentId, commentId)
                .eq(CommentLike::getUserId, userId);
        CommentLike existingLike = commentLikeMapper.selectOne(queryWrapper);
        
        if (existingLike != null) {
            // 取消点赞
            commentLikeMapper.deleteById(existingLike.getId());
            
            // 更新评论的点赞数（减1）
            if (comment.getLikeCount() != null && comment.getLikeCount() > 0) {
                comment.setLikeCount(comment.getLikeCount() - 1);
            } else {
                comment.setLikeCount(0);
            }
            commentMapper.updateById(comment);
            
            log.info("取消点赞成功，评论ID: {}", commentId);
        } else {
            // 点赞
            CommentLike like = new CommentLike();
            like.setCommentId(commentId);
            like.setUserId(userId);
            commentLikeMapper.insert(like);
            
            // 更新评论的点赞数（加1）
            comment.setLikeCount(comment.getLikeCount() != null ? comment.getLikeCount() + 1 : 1);
            commentMapper.updateById(comment);
            
            log.info("点赞成功，评论ID: {}", commentId);
        }
        
        // 3. 返回最新的点赞数
        CommentLikeResponse response = new CommentLikeResponse();
        response.setLikeCount(comment.getLikeCount());
        
        return response;
    }
}
