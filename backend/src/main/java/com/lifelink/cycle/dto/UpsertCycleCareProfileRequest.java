package com.lifelink.cycle.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpsertCycleCareProfileRequest {

    private Long defaultLoverSpaceId;

    @Min(value = 15, message = "Cycle length must be at least 15 days")
    @Max(value = 60, message = "Cycle length must be at most 60 days")
    private Integer cycleLength;

    @Min(value = 1, message = "Period length must be at least 1 day")
    @Max(value = 15, message = "Period length must be at most 15 days")
    private Integer periodLength;

    private LocalDate lastPeriodStartDate;

    private Boolean reminderEnabled;

    private String shareLevel;

    private String timezone;
}
