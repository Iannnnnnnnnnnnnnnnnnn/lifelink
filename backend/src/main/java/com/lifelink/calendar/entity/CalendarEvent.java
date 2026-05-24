package com.lifelink.calendar.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("calendar_events")
public class CalendarEvent {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long relationshipId;

    private String title;

    private String description;

    private String eventType;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Boolean allDay;

    private String repeatType;

    private Integer reminderMinutes;

    private String color;

    private Long createdBy;

    private Long updatedBy;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
