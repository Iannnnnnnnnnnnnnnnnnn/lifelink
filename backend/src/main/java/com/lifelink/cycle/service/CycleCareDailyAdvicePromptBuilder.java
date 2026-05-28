package com.lifelink.cycle.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelink.cycle.entity.CycleDailyLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CycleCareDailyAdvicePromptBuilder {

    public static final String PROMPT_VERSION = "CYCLE_DAILY_ADVICE_V1";

    private static final int MAX_NOTE_LENGTH = 100;

    private final ObjectMapper objectMapper;

    public String buildSystemPrompt() {
        return """
                你是 LifeLink 的周期关怀日报助手。根据脱敏后的结构化生理周期数据，生成温和、具体、生活化的每日关怀建议。
                这不是医学诊断，不提供治疗方案，不推荐具体药物，不做怀孕判断，不作为避孕或备孕依据。
                必须返回 JSON，不要返回 Markdown，不要输出多余文本。
                字段必须完整：
                summary, bodyStatusSummary, flowSummary, painSummary, moodSummary, symptomSummary,
                clothingAdvice, foodAdvice, restAdvice, moodAdvice, partnerAdvice, warningSummary,
                riskLevel, disclaimer。
                riskLevel 只能是 NONE、LOW、MEDIUM、HIGH。
                语气温和克制，不制造焦虑。严重疼痛、大量出血、持续异常时，只能建议咨询医生。
                """;
    }

    public String buildUserPrompt(LocalDate reportDate, CycleDailyLog log, CycleDailyAdviceDraft ruleDraft) {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("promptVersion", PROMPT_VERSION);
        payload.put("reportDate", reportDate == null ? null : reportDate.toString());
        payload.put("phase", ruleDraft.getPhase());
        payload.put("phaseLabel", ruleDraft.getPhaseLabel());
        payload.put("predictedPhase", ruleDraft.getPredictedPhase());
        payload.put("flowLevel", log == null ? null : log.getFlowLevel());
        payload.put("painLevel", log == null ? null : log.getPainLevel());
        payload.put("mood", log == null ? null : log.getMood());
        payload.put("symptoms", readSymptoms(log == null ? null : log.getSymptoms()));
        payload.put("temperatureFeeling", log == null ? null : log.getTemperatureFeeling());
        payload.put("appetite", log == null ? null : log.getAppetite());
        payload.put("noteSummary", sanitizeNote(log == null ? null : log.getNote()));
        payload.put("warningTypes", ruleDraft.getWarningTypes());
        Map<String, Object> ruleBasedDraft = new LinkedHashMap<String, Object>();
        ruleBasedDraft.put("summary", ruleDraft.getSummary());
        ruleBasedDraft.put("bodyStatusSummary", ruleDraft.getBodyStatusSummary());
        ruleBasedDraft.put("flowSummary", ruleDraft.getFlowSummary());
        ruleBasedDraft.put("painSummary", ruleDraft.getPainSummary());
        ruleBasedDraft.put("moodSummary", ruleDraft.getMoodSummary());
        ruleBasedDraft.put("symptomSummary", ruleDraft.getSymptomSummary());
        ruleBasedDraft.put("clothingAdvice", ruleDraft.getClothingAdvice());
        ruleBasedDraft.put("foodAdvice", ruleDraft.getFoodAdvice());
        ruleBasedDraft.put("restAdvice", ruleDraft.getRestAdvice());
        ruleBasedDraft.put("moodAdvice", ruleDraft.getMoodAdvice());
        ruleBasedDraft.put("partnerAdvice", ruleDraft.getPartnerAdvice());
        ruleBasedDraft.put("warningSummary", ruleDraft.getWarningSummary());
        ruleBasedDraft.put("riskLevel", ruleDraft.getRiskLevel());
        payload.put("ruleBasedDraft", ruleBasedDraft);
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            return "{}";
        }
    }

    private List<String> readSymptoms(String symptoms) {
        if (!StringUtils.hasText(symptoms)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(symptoms, new TypeReference<List<String>>() {
            });
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    private String sanitizeNote(String note) {
        if (!StringUtils.hasText(note)) {
            return null;
        }
        String sanitized = note.trim()
                .replaceAll("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}", "[email]")
                .replaceAll("1[3-9]\\d{9}", "[phone]")
                .replaceAll("\\+?\\d[\\d\\s\\-]{6,}\\d", "[phone]");
        if (sanitized.length() <= MAX_NOTE_LENGTH) {
            return sanitized;
        }
        return sanitized.substring(0, MAX_NOTE_LENGTH);
    }
}
