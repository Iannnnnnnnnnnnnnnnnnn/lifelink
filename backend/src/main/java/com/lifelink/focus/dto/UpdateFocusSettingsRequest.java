package com.lifelink.focus.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateFocusSettingsRequest {

    @Min(value = 1, message = "focusMinutes must be at least 1")
    @Max(value = 240, message = "focusMinutes must be at most 240")
    private Integer focusMinutes;

    @Min(value = 1, message = "shortBreakMinutes must be at least 1")
    @Max(value = 120, message = "shortBreakMinutes must be at most 120")
    private Integer shortBreakMinutes;

    @Min(value = 1, message = "longBreakMinutes must be at least 1")
    @Max(value = 240, message = "longBreakMinutes must be at most 240")
    private Integer longBreakMinutes;

    @Min(value = 1, message = "longBreakInterval must be at least 1")
    @Max(value = 12, message = "longBreakInterval must be at most 12")
    private Integer longBreakInterval;

    private Boolean autoStartBreak;

    private Boolean autoStartNextFocus;

    private Boolean soundEnabled;

    private Boolean notificationEnabled;

    private Boolean strictModeEnabled;
}
