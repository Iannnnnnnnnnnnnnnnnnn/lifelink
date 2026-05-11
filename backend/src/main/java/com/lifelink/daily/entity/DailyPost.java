package com.lifelink.daily.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("daily_posts")
public class DailyPost {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long relationshipId;

    private Long userId;

    private String content;

    private String mood;

    private String visibility;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
