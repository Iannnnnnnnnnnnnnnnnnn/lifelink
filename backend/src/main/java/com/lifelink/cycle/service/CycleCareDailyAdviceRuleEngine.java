package com.lifelink.cycle.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelink.cycle.entity.CycleCareProfile;
import com.lifelink.cycle.entity.CycleDailyLog;
import com.lifelink.cycle.entity.CyclePeriodRecord;
import com.lifelink.cycle.entity.CycleWarning;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CycleCareDailyAdviceRuleEngine {

    private static final String FLOW_NONE = "NONE";
    private static final String FLOW_VERY_HEAVY = "VERY_HEAVY";
    private static final String SHARE_PRIVATE = "PRIVATE";
    private static final String SHARE_CALENDAR_ONLY = "CALENDAR_ONLY";

    private final CycleCareAdviceService adviceService;
    private final ObjectMapper objectMapper;

    public CycleDailyAdviceDraft build(CycleCareProfile profile, LocalDate targetDate, CycleDailyLog log,
                                       List<CyclePeriodRecord> records, CyclePredictionResult prediction,
                                       List<CycleWarning> warnings, boolean hasSourceData) {
        CycleDailyAdviceDraft draft = new CycleDailyAdviceDraft();
        String phase = prediction == null || !StringUtils.hasText(prediction.getPhase())
                ? CycleCarePredictionService.UNKNOWN
                : prediction.getPhase();
        CyclePeriodRecord actualPeriod = findActualPeriod(records, targetDate);
        CycleCareAdvice advice = adviceService.buildAdvice(phase, log);

        draft.setReportDate(targetDate);
        draft.setPhase(phase);
        draft.setPhaseLabel(phaseLabel(phase));
        draft.setPredictedPhase(actualPeriod == null && !CycleCarePredictionService.UNKNOWN.equals(phase));
        draft.setShareLevelSnapshot(normalizeShareLevel(profile == null ? null : profile.getShareLevel()));
        draft.setFlowSummary(flowSummary(log));
        draft.setPainSummary(painSummary(log));
        draft.setMoodSummary(moodSummary(log));
        draft.setSymptomSummary(symptomSummary(log));
        draft.setBodyStatusSummary(bodyStatusSummary(log, hasSourceData));
        draft.setClothingAdvice(advice.getClothingAdvice());
        draft.setFoodAdvice(advice.getFoodAdvice());
        draft.setRestAdvice(advice.getRestAdvice());
        draft.setMoodAdvice(advice.getMoodAdvice());
        draft.setPartnerAdvice(advice.getPartnerAdvice());

        Set<String> warningTypes = new LinkedHashSet<String>();
        String riskLevel = "NONE";
        if (warnings != null) {
            for (CycleWarning warning : warnings) {
                if (StringUtils.hasText(warning.getWarningType())) {
                    warningTypes.add(warning.getWarningType());
                    riskLevel = maxRisk(riskLevel, warning.getSeverity());
                }
            }
        }
        if (log != null && log.getPainLevel() != null && log.getPainLevel() >= 8) {
            warningTypes.add("SEVERE_PAIN");
            riskLevel = maxRisk(riskLevel, "HIGH");
        }
        if (log != null && FLOW_VERY_HEAVY.equals(log.getFlowLevel())) {
            warningTypes.add("VERY_HEAVY_FLOW");
            riskLevel = maxRisk(riskLevel, "HIGH");
        }
        if (actualPeriod != null && actualPeriod.getStartDate() != null
                && ChronoUnit.DAYS.between(actualPeriod.getStartDate(), targetDate) + 1 > 7) {
            warningTypes.add("LONG_PERIOD");
            riskLevel = maxRisk(riskLevel, "HIGH");
        }
        if (hasIrregularCycle(records, targetDate)) {
            warningTypes.add("IRREGULAR_CYCLE");
            riskLevel = maxRisk(riskLevel, "MEDIUM");
        }

        draft.setWarningTypes(warningTypes);
        draft.setRiskLevel(riskLevel);
        draft.setSummary(summary(draft, hasSourceData));
        draft.setWarningSummary(warningSummary(warningTypes));
        draft.setPartnerVisibleSummary(partnerVisibleSummary(draft));
        return draft;
    }

    private String summary(CycleDailyAdviceDraft draft, boolean hasSourceData) {
        if (!hasSourceData) {
            return "昨天没有记录到详细状态，可以补充记录，以便生成更贴近你的建议。";
        }
        if (!draft.getWarningTypes().isEmpty()) {
            return "昨天的记录里有需要留意的身体信号，今天适合把节奏放慢一点。";
        }
        if (CycleCarePredictionService.MENSTRUATION.equals(draft.getPhase())) {
            return "昨天处在生理期相关阶段，今天继续以保暖、休息和舒适为主。";
        }
        if (CycleCarePredictionService.LUTEAL.equals(draft.getPhase())) {
            return "昨天接近经前阶段，今天可以多照顾睡眠、饮食和情绪节奏。";
        }
        return "昨天整体没有明显预警，今天继续保持规律记录和舒适节奏。";
    }

    private String bodyStatusSummary(CycleDailyLog log, boolean hasSourceData) {
        if (!hasSourceData) {
            return "昨天没有记录到详细身体状态。";
        }
        if (log == null) {
            return "昨天没有单独填写每日状态，但可结合周期阶段继续温和观察。";
        }
        List<String> parts = new ArrayList<String>();
        parts.add(flowSummary(log));
        parts.add(painSummary(log));
        if (StringUtils.hasText(log.getTemperatureFeeling())) {
            parts.add("体感：" + log.getTemperatureFeeling() + "。");
        }
        if (StringUtils.hasText(log.getAppetite())) {
            parts.add("胃口：" + log.getAppetite() + "。");
        }
        return String.join("", parts);
    }

    private String flowSummary(CycleDailyLog log) {
        if (log == null || !StringUtils.hasText(log.getFlowLevel()) || FLOW_NONE.equals(log.getFlowLevel())) {
            return "昨天没有记录到明显经量。";
        }
        if (FLOW_VERY_HEAVY.equals(log.getFlowLevel())) {
            return "昨天记录的经量偏多，需要留意是否明显多于平时。";
        }
        return "昨天记录的经量为 " + log.getFlowLevel() + "，可以继续结合个人平时情况观察。";
    }

    private String painSummary(CycleDailyLog log) {
        if (log == null || log.getPainLevel() == null || log.getPainLevel() <= 0) {
            return "昨天没有记录明显疼痛。";
        }
        if (log.getPainLevel() >= 8) {
            return "昨天疼痛等级较高，建议今天减少高强度活动，必要时咨询医生。";
        }
        if (log.getPainLevel() >= 5) {
            return "昨天有中等程度疼痛，今天可以继续注意保暖和休息。";
        }
        return "昨天有轻微疼痛记录，可以继续观察身体变化。";
    }

    private String moodSummary(CycleDailyLog log) {
        if (log == null || !StringUtils.hasText(log.getMood())) {
            return "昨天没有记录具体情绪。";
        }
        return "昨天记录的情绪是 " + log.getMood() + "，可以把它当作身体状态的一部分来看待。";
    }

    private String symptomSummary(CycleDailyLog log) {
        List<String> symptoms = readSymptoms(log == null ? null : log.getSymptoms());
        if (symptoms.isEmpty()) {
            return "昨天没有记录明显症状。";
        }
        return "昨天记录了 " + String.join("、", symptoms) + " 等症状，今天可以继续观察是否缓解。";
    }

    private String warningSummary(Set<String> warningTypes) {
        if (warningTypes == null || warningTypes.isEmpty()) {
            return "暂无明显异常预警，继续记录和观察即可。";
        }
        List<String> messages = new ArrayList<String>();
        if (warningTypes.contains("SEVERE_PAIN")) {
            messages.add("疼痛程度较高，建议休息和保暖；如果疼痛剧烈或反复出现，建议咨询医生。");
        }
        if (warningTypes.contains("VERY_HEAVY_FLOW")) {
            messages.add("如果出血量明显多于平时，或短时间内频繁更换卫生用品，建议及时咨询医生。");
        }
        if (warningTypes.contains("LONG_PERIOD")) {
            messages.add("这次出血持续时间较长，建议关注身体状态；如持续不适或出血较多，建议咨询医生。");
        }
        if (warningTypes.contains("IRREGULAR_CYCLE")) {
            messages.add("本次周期与常见范围有差异，建议继续记录；如果多次出现明显不规律，建议咨询医生。");
        }
        if (messages.isEmpty()) {
            messages.add("昨天的记录显示有需要关注的地方，建议继续观察；如持续不适可咨询医生。");
        }
        return String.join("", messages);
    }

    private String partnerVisibleSummary(CycleDailyAdviceDraft draft) {
        String shareLevel = normalizeShareLevel(draft.getShareLevelSnapshot());
        if (SHARE_PRIVATE.equals(shareLevel) || SHARE_CALENDAR_ONLY.equals(shareLevel)) {
            return null;
        }
        if ("BASIC".equals(shareLevel) || "SUMMARY".equals(shareLevel)) {
            return "对方今天可能需要更多关心，可以主动分担一些事情，温柔表达关心。";
        }
        if (!draft.getWarningTypes().isEmpty()) {
            return "对方今天可能需要更多休息和照顾，可以主动分担家务、准备热水或清淡饮食，并避免催促。";
        }
        return draft.getPartnerAdvice();
    }

    private CyclePeriodRecord findActualPeriod(List<CyclePeriodRecord> records, LocalDate targetDate) {
        if (records == null || targetDate == null) {
            return null;
        }
        for (CyclePeriodRecord record : records) {
            LocalDate start = record.getStartDate();
            LocalDate end = record.getEndDate() == null ? targetDate : record.getEndDate();
            if (start != null && !targetDate.isBefore(start) && !targetDate.isAfter(end)) {
                return record;
            }
        }
        return null;
    }

    private boolean hasIrregularCycle(List<CyclePeriodRecord> records, LocalDate targetDate) {
        List<CyclePeriodRecord> sorted = records == null ? new ArrayList<CyclePeriodRecord>() : new ArrayList<CyclePeriodRecord>(records);
        sorted.sort(Comparator.comparing(CyclePeriodRecord::getStartDate, Comparator.nullsLast(Comparator.reverseOrder())));
        for (int i = 0; i + 1 < sorted.size(); i++) {
            CyclePeriodRecord current = sorted.get(i);
            CyclePeriodRecord previous = sorted.get(i + 1);
            if (current.getStartDate() == null || previous.getStartDate() == null || !current.getStartDate().equals(targetDate)) {
                continue;
            }
            long length = ChronoUnit.DAYS.between(previous.getStartDate(), current.getStartDate());
            return length > 0 && (length < 21 || length > 35);
        }
        return false;
    }

    private List<String> readSymptoms(String symptoms) {
        if (!StringUtils.hasText(symptoms)) {
            return new ArrayList<String>();
        }
        try {
            return objectMapper.readValue(symptoms, new TypeReference<List<String>>() {
            });
        } catch (Exception ex) {
            return new ArrayList<String>();
        }
    }

    private String phaseLabel(String phase) {
        if (CycleCarePredictionService.MENSTRUATION.equals(phase)) {
            return "生理期";
        }
        if (CycleCarePredictionService.FOLLICULAR.equals(phase)) {
            return "卵泡期";
        }
        if (CycleCarePredictionService.OVULATION.equals(phase)) {
            return "排卵期附近";
        }
        if (CycleCarePredictionService.LUTEAL.equals(phase)) {
            return "黄体期";
        }
        return "待完善";
    }

    private String normalizeShareLevel(String shareLevel) {
        return StringUtils.hasText(shareLevel) ? shareLevel.trim().toUpperCase() : SHARE_PRIVATE;
    }

    private String maxRisk(String current, String candidate) {
        return riskRank(candidate) > riskRank(current) ? normalizeRisk(candidate) : normalizeRisk(current);
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
}
