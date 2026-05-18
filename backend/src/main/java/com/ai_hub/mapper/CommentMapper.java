package com.ai_hub.mapper;

import com.ai_hub.entity.Comment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评论 Mapper 接口
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    //继承BaseMapper：提供了基本的增删改查方法
}
