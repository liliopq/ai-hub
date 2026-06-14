package com.ai_hub.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举类
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 成功
    SUCCESS(200, "success"),

    // 客户端错误 (4xx)
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "没有权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    CONFLICT(409, "资源冲突"),
    VALIDATION_ERROR(422, "数据验证失败"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),

    // 服务端错误 (5xx)
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),

    // 业务错误 (自定义)
    USER_ALREADY_EXISTS(1001, "用户已存在"),
    USER_NOT_FOUND(1002, "用户不存在"),
    USERNAME_OR_PASSWORD_ERROR(1003, "用户名或密码错误"),
    TOKEN_INVALID(1004, "Token 无效或已过期"),
    TOKEN_EXPIRED(1005, "Token 已过期"),
    TOKEN_VERSION_MISMATCH(1009, "Token 版本不匹配，请重新登录"),
    EMAIL_ALREADY_EXISTS(1006, "邮箱已被注册"),
    OLD_PASSWORD_ERROR(1007, "原密码错误"),
    ACCOUNT_DISABLED(1008, "账号已被禁用"),

    POST_NOT_FOUND(2001, "帖子不存在"),
    POST_ALREADY_LIKED(2002, "已经点赞过了"),
    POST_NOT_LIKED(2003, "还未点赞"),
    POST_TITLE_INVALID(2004, "帖子标题不符合要求"),
    POST_CONTENT_INVALID(2005, "帖子内容不符合要求"),

    COMMENT_NOT_FOUND(3001, "评论不存在"),
    COMMENT_ALREADY_LIKED(3002, "已经点赞过了"),
    COMMENT_NOT_LIKED(3003, "还未点赞"),

    FILE_UPLOAD_ERROR(4001, "文件上传失败"),
    FILE_TYPE_NOT_ALLOWED(4002, "不支持的文件类型"),
    FILE_SIZE_EXCEEDED(4003, "文件大小超出限制");

    private final Integer code;
    private final String message;
}
