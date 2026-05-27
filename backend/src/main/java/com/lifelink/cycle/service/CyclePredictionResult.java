package com.lifelink.cycle.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CyclePredictionResult {

    private String phase;

    private LocalDate predictedNextStartDate;

    private LocalDate predictedNextEndDate;

    private Integer averageCycleLength;

    private Integer averagePeriodLength;

    private Integer daysToNextPeriod;

    private Boolean predictedPeriod;

    private Boolean basedOnHistory;
}
