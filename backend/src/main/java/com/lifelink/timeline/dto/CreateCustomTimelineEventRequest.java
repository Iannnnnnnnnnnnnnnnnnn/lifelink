package com.lifelink.timeline.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateCustomTimelineEventRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title length must be at most 200")
    private String title;

    private String description;

    @NotNull(message = "Event date is required")
    private LocalDateTime eventDate;

    private Long coverFileId;

    private String importance;
}
