package com.ai_hub.mapper;

import com.ai_hub.entity.UserFollow;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户关注 Mapper
 */
@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {

    /**
     * 检查是否已关注
     */
    @Select("SELECT COUNT(*) FROM user_follow WHERE follower_id = #{followerId} AND followee_id = #{followeeId}")
    Long checkFollow(@Param("followerId") Long followerId, @Param("followeeId") Long followeeId);

    /**
     * 获取关注数量
     */
    @Select("SELECT COUNT(*) FROM user_follow WHERE follower_id = #{userId}")
    Long getFollowingCount(@Param("userId") Long userId);

    /**
     * 获取粉丝数量
     */
    @Select("SELECT COUNT(*) FROM user_follow WHERE followee_id = #{userId}")
    Long getFollowerCount(@Param("userId") Long userId);

    /**
     * 获取关注列表
     */
    @Select("SELECT followee_id FROM user_follow WHERE follower_id = #{userId}")
    List<Long> getFollowingList(@Param("userId") Long userId);

    /**
     * 获取粉丝列表
     */
    @Select("SELECT follower_id FROM user_follow WHERE followee_id = #{userId}")
    List<Long> getFollowerList(@Param("userId") Long userId);
}
