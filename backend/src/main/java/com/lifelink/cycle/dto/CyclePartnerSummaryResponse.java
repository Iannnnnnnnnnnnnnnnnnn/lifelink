package com.lifelink.cycle.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CyclePartnerSummaryResponse {

    private Boolean visible;

    private String shareLevel;

    private String title;

    private String careAdvice;

    private String disclaimer;
}
