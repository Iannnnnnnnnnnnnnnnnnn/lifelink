package com.lifelink.daily.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("daily_post_images")
public class DailyPostImage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long dailyPostId;

    private Long fileId;

    private Integer sortOrder;

    private LocalDateTime createdAt;
}
