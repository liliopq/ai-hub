package com.ai_hub.service;

import com.ai_hub.dto.request.AdminPostListRequest;
import com.ai_hub.dto.request.AdminUserListRequest;
import com.ai_hub.dto.response.AdminPostResponse;
import com.ai_hub.dto.response.AdminUserResponse;
import com.ai_hub.dto.response.PageResult;

/**
 * 管理员服务
 */
public interface AdminService {
    
    /**
     * 分页查询用户列表
     *
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<AdminUserResponse> getUserList(AdminUserListRequest request);

    /**
     * 封禁/解封用户
     *
     * @param userId 用户ID
     * @param status 状态 (0:封禁, 1:正常)
     */
    void updateUserStatus(Long userId, Integer status);

    /**
     * 分页查询所有帖子（含待审核）
     *
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<AdminPostResponse> getPostList(AdminPostListRequest request);

    /**
     * 审核帖子（通过/驳回/删除）
     *
     * @param postId 帖子ID
     * @param status 审核状态 (1:通过, 0:驳回, 2:删除)
     * @param reason 审核原因（可选）
     */
    void auditPost(Long postId, Integer status, String reason);

    /**
     * 置顶/取消置顶帖子
     *
     * @param postId 帖子ID
     * @param sticky 是否置顶 (true:置顶, false:取消置顶)
     */
    void updatePostSticky(Long postId, Boolean sticky);

    /**
     * 加精/取消加精帖子
     *
     * @param postId 帖子ID
     * @param essence 是否加精 (true:加精, false:取消加精)
     */
    void updatePostEssence(Long postId, Boolean essence);

    /**
     * 删除任何评论
     *
     * @param commentId 评论ID
     */
    void deleteComment(Long commentId);
}
