package com.lifelink.notification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lifelink.common.typehandler.JsonbStringTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "notifications", autoResultMap = true)
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long receiverUserId;
    private Long actorUserId;
    private String notificationType;
    private String title;
    private String content;
    private String relatedType;
    private Long relatedId;
    private Long relationshipId;
    private String readStatus;
    private String status;

    @TableField(typeHandler = JsonbStringTypeHandler.class)
    private String metadata;

    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
