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
