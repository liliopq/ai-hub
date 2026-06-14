package com.ai_hub.mapper;

import com.ai_hub.entity.AiMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * AI 消息 Mapper
 */
@Mapper
public interface AiMessageMapper extends BaseMapper<AiMessage> {

    /**
     * 查询会话的最近 N 条消息（按序号升序）
     */
    @Select("SELECT * FROM ai_message WHERE session_id = #{sessionId} ORDER BY seq ASC LIMIT #{limit}")
    List<AiMessage> selectRecentBySessionId(@Param("sessionId") String sessionId, @Param("limit") int limit);

    /**
     * 获取会话的最大消息序号
     */
    @Select("SELECT COALESCE(MAX(seq), 0) FROM ai_message WHERE session_id = #{sessionId}")
    int getMaxSeq(@Param("sessionId") String sessionId);

    /**
     * 删除会话的所有消息
     */
    @org.apache.ibatis.annotations.Delete("DELETE FROM ai_message WHERE session_id = #{sessionId}")
    int deleteBySessionId(@Param("sessionId") String sessionId);
}
