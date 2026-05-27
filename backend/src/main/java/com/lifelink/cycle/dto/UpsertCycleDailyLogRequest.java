package com.lifelink.cycle.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpsertCycleDailyLogRequest {

    private Long loverSpaceId;

    private String flowLevel;

    @Min(value = 0, message = "Pain level must be between 0 and 10")
    @Max(value = 10, message = "Pain level must be between 0 and 10")
    private Integer painLevel;

    private String mood;

    private List<String> symptoms;

    private String temperatureFeeling;

    private String appetite;

    @Size(max = 1000, message = "Note length must be at most 1000")
    private String note;
}
