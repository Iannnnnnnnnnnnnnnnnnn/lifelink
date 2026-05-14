package com.lifelink.timeline.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lifelink.common.typehandler.JsonbStringTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "relationship_timeline_events", autoResultMap = true)
public class RelationshipTimelineEvent {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long relationshipId;

    private String eventType;

    private String title;

    private String description;

    private Long actorUserId;

    private String targetType;

    private Long targetId;

    private Long coverFileId;

    private String coverUrl;

    private LocalDateTime eventDate;

    private String importance;

    private String source;

    @TableField(typeHandler = JsonbStringTypeHandler.class)
    private String metadata;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
