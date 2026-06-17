package com.lifelink.cycle.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CycleDailyAdviceReportResponse {

    private Long id;

    private Long loverSpaceId;

    private LocalDate reportDate;

    private String phase;

    private String phaseLabel;

    private Boolean predictedPhase;

    private String summary;

    private String bodyStatusSummary;

    private String flowSummary;

    private String painSummary;

    private String moodSummary;

    private String symptomSummary;

    private String clothingAdvice;

    private String foodAdvice;

    private String restAdvice;

    private String moodAdvice;

    private String partnerAdvice;

    private String warningSummary;

    private String riskLevel;

    private List<String> warningTypes;

    private String shareLevel;

    private String partnerVisibleSummary;

    private String sourceType;

    private Boolean aiGenerated;

    private String status;

    private String disclaimer;

    private Boolean partnerView;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
