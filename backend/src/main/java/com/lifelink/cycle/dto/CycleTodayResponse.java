package com.lifelink.cycle.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CycleTodayResponse {

    private String phase;

    private String phaseLabel;

    private Integer daysToNextPeriod;

    private Boolean predicted;

    private Boolean predictedPeriod;

    private LocalDate predictedNextStartDate;

    private LocalDate predictedNextEndDate;

    private String title;

    private String reminder;

    private String clothingAdvice;

    private String foodAdvice;

    private String restAdvice;

    private String moodAdvice;

    private String partnerAdvice;

    private String disclaimer;

    private List<CycleWarningResponse> warnings;
}
