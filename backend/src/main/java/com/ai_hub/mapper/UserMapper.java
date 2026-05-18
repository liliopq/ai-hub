package com.ai_hub.mapper;

import com.ai_hub.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper 接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    //继承BaseMapper：提供了基本的增删改查方法
}
