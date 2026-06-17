package com.lifelink.focus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("focus_rooms")
public class FocusRoom {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long creatorUserId;

    private Long spaceId;

    private String title;

    private Integer plannedMinutes;

    private String status;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
