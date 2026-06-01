package com.lifelink.focus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("focus_sessions")
public class FocusSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long spaceId;

    private Long todoId;

    private Long roomId;

    private String sessionType;

    private String phase;

    private Integer plannedMinutes;

    private Integer actualMinutes;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    private Integer pausedSeconds;

    private String status;

    private String source;

    private String note;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
