package com.ai_hub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户表
 */
@Data                                    // 自动生成getter和setter方法
@TableName("user")                       // 指定表名
public class User {
    @TableId(type = IdType.AUTO)        // 指定主键类型为自增
    private Long id;                    // 用户id

    private String username;            // 用户名
    private String password;            // 密码
    private String email;               // 邮箱
    @TableField("phonenumber")          // 指定数据库字段名
    private String phoneNumber;         // 手机号
    private String avatar;              // 头像
    private String role;      // USER, CREATOR, ADMIN
    private Integer status;   // 1正常 0封禁
    private Integer tokenVersion;  // Token 版本号，用于强制失效旧 Token（修改密码时 +1）

    @TableField(fill = FieldFill.INSERT)    // 插入时填充
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)         // 插入和更新时填充
    private LocalDateTime updateTime;

    @TableLogic                    // 逻辑删除
    private Integer deleted;       // 逻辑删除字段，0 表示未删除（正常状态），1 表示已删除
}