package com.ai_hub.controller;

import com.ai_hub.dto.request.*;
import com.ai_hub.dto.response.AdminPostResponse;
import com.ai_hub.dto.response.AdminUserResponse;
import com.ai_hub.dto.response.PageResult;
import com.ai_hub.dto.response.Result;
import com.ai_hub.service.AdminService;
import com.ai_hub.utils.TokenValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员控制器
 */
@Slf4j
@RequestMapping("/api/admin")
@RestController
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final TokenValidator tokenValidator;

    /**
     * 分页查询用户列表
     *
     * @param authorization Authorization头
     * @param page 页码，默认1
     * @param size 每页条数，默认10
     * @param username 用户名（模糊搜索）
     * @param role 角色筛选
     * @param status 状态筛选
     * @return 分页结果
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<AdminUserResponse>> getUserList(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status) {
        log.info("管理员查询用户列表请求");

        // 构建查询请求
        AdminUserListRequest request = new AdminUserListRequest();
        request.setPage(page);
        request.setSize(size);
        request.setUsername(username);
        request.setRole(role);
        request.setStatus(status);

        // 获取用户列表
        PageResult<AdminUserResponse> userList = adminService.getUserList(request);

        return Result.success(userList);
    }

    /**
     * 封禁/解封用户
     *
     * @param authorization Authorization头
     * @param userId 用户ID
     * @param request 状态更新请求
     * @return 操作结果
     */
    @PutMapping("/users/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateUserStatus(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        log.info("封禁/解封用户请求，目标用户ID: {}, 新状态: {}", userId, request.getStatus());

        // 执行状态更新
        adminService.updateUserStatus(userId, request.getStatus());

        String message = request.getStatus() == 0 ? "用户已封禁" : "用户已解封";
        return Result.success(message, null);
    }

    /**
     * 分页查询所有帖子（含待审核）
     *
     * @param authorization Authorization头
     * @param page 页码，默认1
     * @param size 每页条数，默认10
     * @param status 状态筛选
     * @param userId 用户ID筛选
     * @return 分页结果
     */
    @GetMapping("/posts")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<AdminPostResponse>> getPostList(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long userId) {
        log.info("管理员查询帖子列表请求");

        // 构建查询请求
        AdminPostListRequest request = new AdminPostListRequest();
        request.setPage(page);
        request.setSize(size);
        request.setStatus(status);
        request.setUserId(userId);

        // 获取帖子列表
        PageResult<AdminPostResponse> postList = adminService.getPostList(request);

        return Result.success(postList);
    }

    /**
     * 审核帖子（通过/驳回/删除）
     *
     * @param authorization Authorization头
     * @param postId 帖子ID
     * @param request 审核请求
     * @return 操作结果
     */
    @PutMapping("/posts/{postId}/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> auditPost(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long postId,
            @Valid @RequestBody AuditPostRequest request) {
        log.info("审核帖子请求，帖子ID: {}, 新状态: {}", postId, request.getStatus());

        // 执行审核
        adminService.auditPost(postId, request.getStatus(), request.getReason());

        return Result.success("审核完成", null);
    }

    /**
     * 置顶/取消置顶帖子
     *
     * @param authorization Authorization头
     * @param postId 帖子ID
     * @param request 置顶状态请求
     * @return 操作结果
     */
    @PutMapping("/posts/{postId}/sticky")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updatePostSticky(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long postId,
            @Valid @RequestBody UpdatePostStickyRequest request) {
        log.info("更新帖子置顶状态请求，帖子ID: {}, 新状态: {}", postId, request.getSticky());

        // 执行更新
        adminService.updatePostSticky(postId, request.getSticky());

        return Result.success("操作成功", null);
    }

    /**
     * 删除任何评论
     *
     * @param authorization Authorization头
     * @param commentId 评论ID
     * @return 操作结果
     */
    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteComment(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long commentId) {
        log.info("管理员删除评论请求，评论ID: {}", commentId);

        // 执行删除
        adminService.deleteComment(commentId);

        return Result.success("删除成功", null);
    }
}
