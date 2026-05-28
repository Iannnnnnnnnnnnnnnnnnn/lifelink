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

    private String bloodColor;

    @Min(value = 0, message = "Pain level must be between 0 and 10")
    @Max(value = 10, message = "Pain level must be between 0 and 10")
    private Integer painLevel;

    private String mood;

    private List<String> symptoms;

    private String temperatureFeeling;

    private String appetite;

    @Min(value = 0, message = "Sleep hours must be non-negative")
    @Max(value = 24, message = "Sleep hours must be at most 24")
    private Double sleepHours;

    @Min(value = 0, message = "Water cups must be non-negative")
    @Max(value = 40, message = "Water cups must be at most 40")
    private Integer waterCups;

    @Min(value = 0, message = "Exercise minutes must be non-negative")
    @Max(value = 1440, message = "Exercise minutes must be at most 1440")
    private Integer exerciseMinutes;

    private List<String> foodTags;

    @Size(max = 500, message = "Medication note length must be at most 500")
    private String medicationNote;

    @Size(max = 500, message = "Discharge note length must be at most 500")
    private String dischargeNote;

    private Double temperature;

    private Double weight;

    @Size(max = 1000, message = "Note length must be at most 1000")
    private String note;
}
