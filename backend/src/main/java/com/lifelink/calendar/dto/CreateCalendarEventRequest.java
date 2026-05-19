package com.lifelink.calendar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateCalendarEventRequest {

    @NotNull(message = "Relationship is required")
    private Long relationshipId;

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title length must be at most 100")
    private String title;

    private String description;

    @Pattern(regexp = "CUSTOM|REMINDER|PLAN|APPOINTMENT|OTHER", message = "Event type is invalid")
    private String eventType;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Boolean allDay;

    @Pattern(regexp = "NONE|DAILY|WEEKLY|MONTHLY|YEARLY", message = "Repeat type is invalid")
    private String repeatType;

    private Integer reminderMinutes;

    @Size(max = 30, message = "Color length must be at most 30")
    private String color;
}
