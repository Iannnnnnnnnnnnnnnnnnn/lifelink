package com.lifelink.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lifelink.ai.entity.MemoryVector;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MemoryVectorMapper extends BaseMapper<MemoryVector> {

    @Delete("DELETE FROM memory_vector WHERE user_id = #{userId} AND space_id = #{spaceId}")
    int deleteByUserAndSpace(@Param("userId") Long userId, @Param("spaceId") Long spaceId);

    @Insert("INSERT INTO memory_vector (user_id, space_id, source_type, source_id, content, embedding, metadata, created_time) "
            + "VALUES (#{memory.userId}, #{memory.spaceId}, #{memory.sourceType}, #{memory.sourceId}, #{memory.content}, "
            + "CAST(#{embedding} AS vector), CAST(#{memory.metadata} AS jsonb), #{memory.createdTime})")
    int insertMemory(@Param("memory") MemoryVector memory, @Param("embedding") String embedding);

    @Select("SELECT id, user_id, space_id, source_type, source_id, content, metadata, created_time "
            + "FROM memory_vector WHERE user_id = #{userId} AND space_id = #{spaceId} "
            + "ORDER BY embedding <-> CAST(#{embedding} AS vector) LIMIT #{limit}")
    List<MemoryVector> search(@Param("userId") Long userId,
                              @Param("spaceId") Long spaceId,
                              @Param("embedding") String embedding,
                              @Param("limit") int limit);
}
