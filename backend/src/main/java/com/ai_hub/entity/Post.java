package com.ai_hub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 帖子表
 * @author ：aihub
 * @date ：2021-01-05 11:05 上午
 * @description：帖子
 */
@Data
@TableName("post")
public class Post {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;          // 用户ID
    private String title;         // 标题
    private String content;       // 内容
    private String category;      // 分类
    private String tags;          // 标签
    private Integer viewCount;    // 浏览次数
    private Integer likeCount;    // 点赞次数
    private Integer collectCount; // 收藏次数
    private Integer commentCount;  // 评论次数
    private Integer isSticky;    // 是否置顶，0否或1是
    private Integer isEssence;   // 是否精华，0否或1是
    private Integer status;      // 状态，1正常 0审核中 2已删除

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}

