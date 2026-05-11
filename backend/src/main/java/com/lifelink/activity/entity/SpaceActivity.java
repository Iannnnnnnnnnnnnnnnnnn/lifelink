package com.lifelink.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lifelink.common.typehandler.JsonbStringTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "space_activities", autoResultMap = true)
public class SpaceActivity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long relationshipId;

    private Long actorUserId;

    private String activityType;

    private String targetType;

    private Long targetId;

    private String title;

    private String content;

    @TableField(typeHandler = JsonbStringTypeHandler.class)
    private String metadata;

    private String status;

    private LocalDateTime createdAt;
}
