package com.ai_hub.service;

import com.ai_hub.dto.response.UserResponse;

import java.util.List;

/**
 * 关注服务接口
 */
public interface FollowService {

    /**
     * 关注用户
     *
     * @param followerId 关注者ID
     * @param followeeId 被关注者ID
     */
    void follow(Long followerId, Long followeeId);

    /**
     * 取消关注用户
     *
     * @param followerId 关注者ID
     * @param followeeId 被关注者ID
     */
    void unfollow(Long followerId, Long followeeId);

    /**
     * 检查是否已关注
     *
     * @param followerId 关注者ID
     * @param followeeId 被关注者ID
     * @return 是否已关注
     */
    boolean isFollowing(Long followerId, Long followeeId);

    /**
     * 获取关注数量
     *
     * @param userId 用户ID
     * @return 关注数量
     */
    Long getFollowingCount(Long userId);

    /**
     * 获取粉丝数量
     *
     * @param userId 用户ID
     * @return 粉丝数量
     */
    Long getFollowerCount(Long userId);

    /**
     * 获取关注列表
     *
     * @param userId 用户ID
     * @return 关注用户列表
     */
    List<UserResponse> getFollowingList(Long userId);

    /**
     * 获取粉丝列表
     *
     * @param userId 用户ID
     * @return 粉丝用户列表
     */
    List<UserResponse> getFollowerList(Long userId);
}
