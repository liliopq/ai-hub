package com.ai_hub.dto.request;

import lombok.Data;

/**
 * 管理员查询用户列表请求
 */
@Data
public class AdminUserListRequest {
    
    /**
     * 页码，默认1
     */
    private Integer page = 1;
    
    /**
     * 每页条数，默认10
     */
    private Integer size = 10;
    
    /**
     * 用户名（模糊搜索）
     */
    private String username;
    
    /**
     * 角色筛选 (USER/CREATOR/ADMIN)
     */
    private String role;
    
    /**
     * 状态筛选 (1:正常, 0:封禁)
     */
    private Integer status;
}
