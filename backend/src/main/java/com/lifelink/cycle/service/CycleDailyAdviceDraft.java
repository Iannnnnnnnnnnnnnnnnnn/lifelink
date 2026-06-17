package com.lifelink.cycle.service;

import lombok.Data;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class CycleDailyAdviceDraft {

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

    private String riskLevel = "NONE";

    private Set<String> warningTypes = new LinkedHashSet<String>();

    private String shareLevelSnapshot;

    private String partnerVisibleSummary;

    private String sourceType = "RULE_BASED";

    private Boolean aiGenerated = false;

    private String aiModel;

    private String promptVersion = CycleCareDailyAdvicePromptBuilder.PROMPT_VERSION;

    private String rawAiResponse;
}
