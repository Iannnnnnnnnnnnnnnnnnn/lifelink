package com.lifelink.focus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FocusSettingsResponse {

    private Long id;

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
