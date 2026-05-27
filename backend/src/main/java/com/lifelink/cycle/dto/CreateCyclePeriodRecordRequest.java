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

    @Size(max = 1000, message = "Note length must be at most 1000")
    private String note;
}
