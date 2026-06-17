package com.lifelink.cycle.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CycleParseLogResponse {

    private String flowLevel;

    private String bloodColor;

    private Integer painLevel;

    private String mood;

    private Double sleepHours;

    private Integer waterCups;

    private Integer exerciseMinutes;

    private List<String> symptoms;

    private List<String> foodTags;

    private String note;

    private Boolean ruleBased;

    private String disclaimer;
}
