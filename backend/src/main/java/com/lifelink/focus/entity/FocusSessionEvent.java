package com.lifelink.focus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lifelink.common.typehandler.JsonbStringTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "focus_session_events", autoResultMap = true)
public class FocusSessionEvent {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;

    private Long userId;

    private String eventType;

    private LocalDateTime eventTime;

    @TableField(typeHandler = JsonbStringTypeHandler.class)
    private String metadata;

    private LocalDateTime createdAt;
}
