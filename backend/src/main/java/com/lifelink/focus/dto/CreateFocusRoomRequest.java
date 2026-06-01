package com.lifelink.focus.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateFocusRoomRequest {

    @NotNull(message = "spaceId is required")
    private Long spaceId;

    @Size(max = 120, message = "title length must be at most 120")
    private String title;

    @Min(value = 1, message = "plannedMinutes must be at least 1")
    @Max(value = 240, message = "plannedMinutes must be at most 240")
    private Integer plannedMinutes;

    private List<Long> inviteUserIds;
}
