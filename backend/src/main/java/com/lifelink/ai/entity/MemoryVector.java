package com.lifelink.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("memory_vector")
public class MemoryVector {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long spaceId;

    private String sourceType;

    private Long sourceId;

    private String content;

    private String embedding;

    private String metadata;

    private LocalDateTime createdTime;
}
