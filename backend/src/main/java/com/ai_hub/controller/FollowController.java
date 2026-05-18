package com.ai_hub.controller;

import com.ai_hub.dto.response.Result;
import com.ai_hub.dto.response.UserResponse;
import com.ai_hub.service.FollowService;
import com.ai_hub.utils.TokenValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 关注控制器
 */
@Slf4j
@RequestMapping("/api/follow")
@RestController
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;
    private final TokenValidator tokenValidator;

    /**
     * 关注用户
     *
     * @param authorization Authorization头
     * @param followeeId 被关注者ID
     * @return 操作结果
     */
    @PostMapping("/{followeeId}")
    public Result<Void> follow(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long followeeId) {
        log.info("关注用户请求，被关注者ID: {}", followeeId);

        // 验证Token并提取用户ID
        TokenValidator.ValidationResult result = tokenValidator.validateAndExtractUserId(authorization);
        if (!result.isSuccess()) {
            return Result.error(result.getErrorResult().getCode(), result.getErrorResult().getMessage());
        }

        Long followerId = result.getUserId();

        // 执行关注
        followService.follow(followerId, followeeId);

        return Result.success("关注成功", null);
    }

    /**
     * 取消关注用户
     *
     * @param authorization Authorization头
     * @param followeeId 被关注者ID
     * @return 操作结果
     */
    @DeleteMapping("/{followeeId}")
    public Result<Void> unfollow(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long followeeId) {
        log.info("取消关注用户请求，被关注者ID: {}", followeeId);

        // 验证Token并提取用户ID
        TokenValidator.ValidationResult result = tokenValidator.validateAndExtractUserId(authorization);
        if (!result.isSuccess()) {
            return Result.error(result.getErrorResult().getCode(), result.getErrorResult().getMessage());
        }

        Long followerId = result.getUserId();

        // 执行取消关注
        followService.unfollow(followerId, followeeId);

        return Result.success("取消关注成功", null);
    }

    /**
     * 检查是否已关注
     *
     * @param authorization Authorization头
     * @param followeeId 被检查者ID
     * @return 是否已关注
     */
    @GetMapping("/check/{followeeId}")
    public Result<Map<String, Boolean>> checkFollowing(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long followeeId) {
        log.info("检查关注状态请求，被检查者ID: {}", followeeId);

        // 验证Token并提取用户ID
        TokenValidator.ValidationResult result = tokenValidator.validateAndExtractUserId(authorization);
        if (!result.isSuccess()) {
            return Result.error(result.getErrorResult().getCode(), result.getErrorResult().getMessage());
        }

        Long followerId = result.getUserId();

        // 检查关注状态
        boolean isFollowing = followService.isFollowing(followerId, followeeId);

        Map<String, Boolean> response = new HashMap<>();
        response.put("isFollowing", isFollowing);

        return Result.success(response);
    }

    /**
     * 获取用户的关注数量和粉丝数量
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    @GetMapping("/count/{userId}")
    public Result<Map<String, Long>> getFollowCounts(@PathVariable Long userId) {
        log.info("获取关注统计请求，用户ID: {}", userId);

        Long followingCount = followService.getFollowingCount(userId);
        Long followerCount = followService.getFollowerCount(userId);

        Map<String, Long> response = new HashMap<>();
        response.put("followingCount", followingCount);
        response.put("followerCount", followerCount);

        return Result.success(response);
    }

    /**
     * 获取用户的关注列表
     *
     * @param userId 用户ID
     * @return 关注用户列表
     */
    @GetMapping("/following/{userId}")
    public Result<List<UserResponse>> getFollowingList(@PathVariable Long userId) {
        log.info("获取关注列表请求，用户ID: {}", userId);

        List<UserResponse> followingList = followService.getFollowingList(userId);

        return Result.success(followingList);
    }

    /**
     * 获取用户的粉丝列表
     *
     * @param userId 用户ID
     * @return 粉丝用户列表
     */
    @GetMapping("/followers/{userId}")
    public Result<List<UserResponse>> getFollowerList(@PathVariable Long userId) {
        log.info("获取粉丝列表请求，用户ID: {}", userId);

        List<UserResponse> followerList = followService.getFollowerList(userId);

        return Result.success(followerList);
    }
}
