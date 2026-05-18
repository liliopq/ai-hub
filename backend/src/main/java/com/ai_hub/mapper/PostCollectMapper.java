package com.ai_hub.mapper;

import com.ai_hub.entity.PostCollect;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 帖子收藏记录 Mapper 接口
 */
@Mapper
public interface PostCollectMapper extends BaseMapper<PostCollect> {
    //继承BaseMapper：提供了基本的增删改查方法
}
