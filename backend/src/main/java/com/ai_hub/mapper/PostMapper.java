package com.ai_hub.mapper;

import com.ai_hub.entity.Post;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 帖子 Mapper 接口
 */
@Mapper
public interface PostMapper extends BaseMapper<Post> {

    /**
     * 增加帖子浏览数
     */
    @Update("UPDATE post SET view_count = view_count + 1 WHERE id = #{postId}")
    int incrementViewCount(Long postId);

    /**
     * 原子增加帖子点赞数
     */
    @Update("UPDATE post SET like_count = like_count + 1 WHERE id = #{postId}")
    int incrementLikeCount(Long postId);

    /**
     * 原子减少帖子点赞数（确保不小于0）
     */
    @Update("UPDATE post SET like_count = GREATEST(like_count - 1, 0) WHERE id = #{postId}")
    int decrementLikeCount(Long postId);

    /**
     * 原子增加帖子收藏数
     */
    @Update("UPDATE post SET collect_count = COALESCE(collect_count, 0) + 1 WHERE id = #{postId}")
    int incrementCollectCount(Long postId);

    /**
     * 原子减少帖子收藏数（确保不小于0）
     */
    @Update("UPDATE post SET collect_count = GREATEST(COALESCE(collect_count, 0) - 1, 0) WHERE id = #{postId}")
    int decrementCollectCount(Long postId);

    /**
     * 使用 MySQL 全文索引搜索帖子（布尔模式）
     * MATCH...AGAINST 比 LIKE '%keyword%' 性能高 10-100 倍
     * IN BOOLEAN MODE 支持 +必须包含 -排除 ~降低权重 等操作符
     */
    @Select("SELECT * FROM post WHERE status = 1 " +
            "AND MATCH(title, content) AGAINST(#{keyword} IN BOOLEAN MODE) " +
            "ORDER BY is_sticky DESC, create_time DESC " +
            "LIMIT #{offset}, #{size}")
    List<Post> searchByFulltext(@Param("keyword") String keyword,
                                 @Param("offset") int offset,
                                 @Param("size") int size);

    /**
     * 全文索引搜索结果总数
     */
    @Select("SELECT COUNT(*) FROM post WHERE status = 1 " +
            "AND MATCH(title, content) AGAINST(#{keyword} IN BOOLEAN MODE)")
    long countByFulltext(@Param("keyword") String keyword);
}
