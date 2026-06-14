package com.ai_hub.security;

import com.ai_hub.entity.User;
import com.ai_hub.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * 自定义权限评估器
 * 用于 @PreAuthorize 注解中的权限表达式
 */
@Component
@RequiredArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final UserMapper userMapper;

    /**
     * 评估用户是否具有指定权限
     */
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Long userId = (Long) authentication.getPrincipal();
        User user = userMapper.selectById(userId);
        
        if (user == null) {
            return false;
        }

        String role = user.getRole();
        String requiredRole = permission.toString();

        // 检查角色匹配（支持 ROLE_ 前缀和不带前缀两种方式）
        if (requiredRole.startsWith("ROLE_")) {
            return requiredRole.equals(role) || requiredRole.equals("ROLE_" + role);
        } else {
            return role.equals(requiredRole) || role.equals("ROLE_" + requiredRole);
        }
    }

    /**
     * 评估用户是否具有指定权限（基于 ID）
     */
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return hasPermission(authentication, null, permission);
    }
}
