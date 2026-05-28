package com.lifelink.cycle.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelink.ai.config.AiProperties;
import com.lifelink.ai.dto.AiChatRequest;
import com.lifelink.ai.dto.AiChatResult;
import com.lifelink.ai.service.AiChatService;
import com.lifelink.cycle.config.CycleCareDailyAdviceProperties;
import com.lifelink.cycle.entity.CycleCareProfile;
import com.lifelink.cycle.entity.CycleDailyLog;
import com.lifelink.cycle.entity.CyclePeriodRecord;
import com.lifelink.cycle.entity.CycleWarning;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CycleCareDailyAdviceGenerator {

    private final CycleCareDailyAdviceRuleEngine ruleEngine;
    private final CycleCareDailyAdvicePromptBuilder promptBuilder;
    private final CycleCareDailyAdviceProperties dailyAdviceProperties;
    private final AiProperties aiProperties;
    private final AiChatService aiChatService;
    private final ObjectMapper objectMapper;

    public CycleDailyAdviceDraft generate(CycleCareProfile profile, LocalDate targetDate, CycleDailyLog log,
                                          List<CyclePeriodRecord> records, CyclePredictionResult prediction,
                                          List<CycleWarning> warnings, boolean hasSourceData) {
        CycleDailyAdviceDraft draft = ruleEngine.build(profile, targetDate, log, records, prediction, warnings, hasSourceData);
        if (!canUseAi()) {
            return draft;
        }
        try {
            AiChatRequest request = new AiChatRequest(
                    promptBuilder.buildSystemPrompt(),
                    promptBuilder.buildUserPrompt(targetDate, log, draft),
                    0.4,
                    1200,
                    true
            );
            AiChatResult result = aiChatService.chat(request);
            JsonNode root = objectMapper.readTree(cleanJson(result.getContent()));
            applyAiJson(draft, root);
            draft.setSourceType("MIXED");
            draft.setAiGenerated(true);
            draft.setAiModel(result.getModel());
            draft.setRawAiResponse(cleanJson(result.getContent()));
        } catch (Exception ex) {
            draft.setSourceType("RULE_BASED");
            draft.setAiGenerated(false);
            draft.setAiModel(null);
            draft.setRawAiResponse(null);
        }
        return draft;
    }

    private boolean canUseAi() {
        return Boolean.TRUE.equals(dailyAdviceProperties.getUseAi())
                && Boolean.TRUE.equals(aiProperties.getEnabled())
                && !Boolean.TRUE.equals(aiProperties.getMock())
                && StringUtils.hasText(aiProperties.getApiKey())
                && !"mock".equalsIgnoreCase(String.valueOf(aiProperties.getProvider()));
    }

    private void applyAiJson(CycleDailyAdviceDraft draft, JsonNode root) {
        draft.setSummary(text(root, "summary", draft.getSummary()));
        draft.setBodyStatusSummary(text(root, "bodyStatusSummary", draft.getBodyStatusSummary()));
        draft.setFlowSummary(text(root, "flowSummary", draft.getFlowSummary()));
        draft.setPainSummary(text(root, "painSummary", draft.getPainSummary()));
        draft.setMoodSummary(text(root, "moodSummary", draft.getMoodSummary()));
        draft.setSymptomSummary(text(root, "symptomSummary", draft.getSymptomSummary()));
        draft.setClothingAdvice(text(root, "clothingAdvice", draft.getClothingAdvice()));
        draft.setFoodAdvice(text(root, "foodAdvice", draft.getFoodAdvice()));
        draft.setRestAdvice(text(root, "restAdvice", draft.getRestAdvice()));
        draft.setMoodAdvice(text(root, "moodAdvice", draft.getMoodAdvice()));
        draft.setPartnerAdvice(text(root, "partnerAdvice", draft.getPartnerAdvice()));
        draft.setWarningSummary(text(root, "warningSummary", draft.getWarningSummary()));
        String aiRisk = normalizeRisk(text(root, "riskLevel", draft.getRiskLevel()));
        if (riskRank(aiRisk) >= riskRank(draft.getRiskLevel())) {
            draft.setRiskLevel(aiRisk);
        }
        draft.setPartnerVisibleSummary(partnerVisibleSummary(draft));
    }

    private String text(JsonNode root, String field, String fallback) {
        JsonNode node = root == null ? null : root.get(field);
        if (node == null || !node.isTextual() || !StringUtils.hasText(node.asText())) {
            return fallback;
        }
        return node.asText().trim();
    }

    private String cleanJson(String content) {
        if (!StringUtils.hasText(content)) {
            return "{}";
        }
        String value = content.trim();
        if (value.startsWith("```")) {
            value = value.replaceFirst("^```json\\s*", "").replaceFirst("^```\\s*", "");
            int end = value.lastIndexOf("```");
            if (end >= 0) {
                value = value.substring(0, end);
            }
        }
        return value.trim();
    }

    private String partnerVisibleSummary(CycleDailyAdviceDraft draft) {
        String shareLevel = String.valueOf(draft.getShareLevelSnapshot()).toUpperCase();
        if ("PRIVATE".equals(shareLevel) || "CALENDAR_ONLY".equals(shareLevel)) {
            return null;
        }
        Set<String> warnings = draft.getWarningTypes();
        if ("BASIC".equals(shareLevel) || "SUMMARY".equals(shareLevel)) {
            return "对方今天可能需要更多关心，可以主动分担一些事情，温柔表达关心。";
        }
        if (warnings != null && !warnings.isEmpty()) {
            return "对方今天可能需要更多休息和照顾，可以主动分担家务、准备热水或清淡饮食，并避免催促。";
        }
        return draft.getPartnerAdvice();
    }

    private String normalizeRisk(String risk) {
        if ("HIGH".equalsIgnoreCase(String.valueOf(risk))) {
            return "HIGH";
        }
        if ("MEDIUM".equalsIgnoreCase(String.valueOf(risk))) {
            return "MEDIUM";
        }
        if ("LOW".equalsIgnoreCase(String.valueOf(risk))) {
            return "LOW";
        }
        return "NONE";
    }

    private int riskRank(String risk) {
        String value = normalizeRisk(risk);
        if ("HIGH".equals(value)) {
            return 3;
        }
        if ("MEDIUM".equals(value)) {
            return 2;
        }
        if ("LOW".equals(value)) {
            return 1;
        }
        return 0;
    }
}
