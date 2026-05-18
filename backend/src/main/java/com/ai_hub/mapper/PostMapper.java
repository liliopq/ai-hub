package com.ai_hub.mapper;

import com.ai_hub.entity.Post;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 帖子 Mapper 接口
 */
@Mapper
public interface PostMapper extends BaseMapper<Post> {
    //继承BaseMapper：提供了基本的增删改查方法
}
