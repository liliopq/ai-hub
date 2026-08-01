package com.ai_hub.mapper;

import com.ai_hub.entity.Comment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * 评论 Mapper 接口
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 原子增加评论点赞数
     */
    @Update("UPDATE comment SET like_count = COALESCE(like_count, 0) + 1 WHERE id = #{commentId}")
    int incrementLikeCount(Long commentId);

    /**
     * 原子减少评论点赞数（确保不小于0）
     */
    @Update("UPDATE comment SET like_count = GREATEST(COALESCE(like_count, 0) - 1, 0) WHERE id = #{commentId}")
    int decrementLikeCount(Long commentId);
}
