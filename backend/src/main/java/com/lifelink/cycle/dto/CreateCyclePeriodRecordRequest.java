package com.lifelink.cycle.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateCyclePeriodRecordRequest {

    private Long loverSpaceId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    @Size(max = 100, message = "Flow summary length must be at most 100")
    private String flowSummary;

    @Size(max = 100, message = "Pain summary length must be at most 100")
    private String painSummary;

    @Size(max = 100, message = "Color summary length must be at most 100")
    private String colorSummary;

    @Size(max = 1000, message = "Note length must be at most 1000")
    private String note;
}
