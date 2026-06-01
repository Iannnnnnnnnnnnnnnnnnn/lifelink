package com.lifelink.focus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("focus_settings")
public class FocusSettings {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer focusMinutes;

    private Integer shortBreakMinutes;

    private Integer longBreakMinutes;

    private Integer longBreakInterval;

    private Boolean autoStartBreak;

    private Boolean autoStartNextFocus;

    private Boolean soundEnabled;

    private Boolean notificationEnabled;

    private Boolean strictModeEnabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
