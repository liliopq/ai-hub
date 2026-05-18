package com.ai_hub.service.impl;

import com.ai_hub.dto.response.UserResponse;
import com.ai_hub.entity.User;
import com.ai_hub.entity.UserFollow;
import com.ai_hub.enums.ErrorCode;
import com.ai_hub.mapper.UserFollowMapper;
import com.ai_hub.mapper.UserMapper;
import com.ai_hub.service.FollowService;
import com.ai_hub.service.WebSocketNotificationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 关注服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final UserFollowMapper userFollowMapper;
    private final UserMapper userMapper;
    private final WebSocketNotificationService webSocketNotificationService;

    @Override
    public void follow(Long followerId, Long followeeId) {
        log.info("关注用户，关注者ID: {}, 被关注者ID: {}", followerId, followeeId);

        // 1. 检查是否是关注自己
        if (followerId.equals(followeeId)) {
            throw new RuntimeException("不能关注自己");
        }

        // 2. 检查被关注者是否存在
        User followee = userMapper.selectById(followeeId);
        if (followee == null) {
            throw new RuntimeException(ErrorCode.USER_NOT_FOUND.getMessage());
        }

        // 3. 检查是否已经关注
        Long count = userFollowMapper.checkFollow(followerId, followeeId);
        if (count > 0) {
            throw new RuntimeException("已经关注该用户");
        }

        // 4. 创建关注记录
        UserFollow userFollow = new UserFollow();
        userFollow.setFollowerId(followerId);
        userFollow.setFolloweeId(followeeId);
        userFollowMapper.insert(userFollow);

        log.info("关注成功，关注者ID: {}, 被关注者ID: {}", followerId, followeeId);

        // 5. 发送关注通知给被关注者
        webSocketNotificationService.sendFollowNotification(followeeId, followerId);
    }

    @Override
    public void unfollow(Long followerId, Long followeeId) {
        log.info("取消关注用户，关注者ID: {}, 被关注者ID: {}", followerId, followeeId);

        // 1. 检查是否存在关注关系
        LambdaQueryWrapper<UserFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFollow::getFollowerId, followerId)
                .eq(UserFollow::getFolloweeId, followeeId);
        
        UserFollow userFollow = userFollowMapper.selectOne(queryWrapper);
        if (userFollow == null) {
            throw new RuntimeException("未关注该用户");
        }

        // 2. 删除关注记录
        userFollowMapper.deleteById(userFollow.getId());

        log.info("取消关注成功，关注者ID: {}, 被关注者ID: {}", followerId, followeeId);
    }

    @Override
    public boolean isFollowing(Long followerId, Long followeeId) {
        Long count = userFollowMapper.checkFollow(followerId, followeeId);
        return count > 0;
    }

    @Override
    public Long getFollowingCount(Long userId) {
        return userFollowMapper.getFollowingCount(userId);
    }

    @Override
    public Long getFollowerCount(Long userId) {
        return userFollowMapper.getFollowerCount(userId);
    }

    @Override
    public List<UserResponse> getFollowingList(Long userId) {
        log.info("获取关注列表，用户ID: {}", userId);

        List<Long> followeeIds = userFollowMapper.getFollowingList(userId);
        if (followeeIds.isEmpty()) {
            return List.of();
        }

        List<User> users = userMapper.selectBatchIds(followeeIds);
        return users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponse> getFollowerList(Long userId) {
        log.info("获取粉丝列表，用户ID: {}", userId);

        List<Long> followerIds = userFollowMapper.getFollowerList(userId);
        if (followerIds.isEmpty()) {
            return List.of();
        }

        List<User> users = userMapper.selectBatchIds(followerIds);
        return users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setAvatar(user.getAvatar());
        response.setRole(user.getRole());
        response.setCreateTime(user.getCreateTime());
        return response;
    }
}
