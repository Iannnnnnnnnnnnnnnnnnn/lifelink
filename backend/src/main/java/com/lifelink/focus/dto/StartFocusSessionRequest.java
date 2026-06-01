package com.lifelink.focus.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class StartFocusSessionRequest {

    private Long spaceId;

    private Long todoId;

    private Long roomId;

    private String phase;

    @Min(value = 1, message = "plannedMinutes must be at least 1")
    @Max(value = 240, message = "plannedMinutes must be at most 240")
    private Integer plannedMinutes;

    private String source;

    private String note;
}
