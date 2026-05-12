package com.lifelink.daily.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("daily_post_likes")
public class DailyPostLike {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long dailyPostId;

    private Long userId;

    private LocalDateTime createdAt;
}
