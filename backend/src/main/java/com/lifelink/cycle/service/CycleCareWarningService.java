package com.lifelink.cycle.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifelink.cycle.entity.CycleCareProfile;
import com.lifelink.cycle.entity.CycleDailyLog;
import com.lifelink.cycle.entity.CyclePeriodRecord;
import com.lifelink.cycle.entity.CycleWarning;
import com.lifelink.cycle.mapper.CycleWarningMapper;
import com.lifelink.notification.service.NotificationService;
import com.lifelink.relationship.entity.RelationshipMember;
import com.lifelink.relationship.service.RelationshipPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CycleCareWarningService {

    public static final String ACTIVE_STATUS = "ACTIVE";
    public static final String DISMISSED_STATUS = "DISMISSED";

    private static final String PRIVATE_SHARE = "PRIVATE";
    private static final String SUMMARY_SHARE = "SUMMARY";
    private static final String FULL_SHARE = "FULL";
    private static final String BASIC_SHARE = "BASIC";
    private static final String DETAILED_SHARE = "DETAILED";
    private static final String FLOW_NONE = "NONE";
    private static final String FLOW_VERY_HEAVY = "VERY_HEAVY";

    private final CycleWarningMapper warningMapper;
    private final NotificationService notificationService;
    private final RelationshipPermissionService relationshipPermissionService;
    private final CycleCarePredictionService predictionService;

    public void generateWarnings(CycleCareProfile profile, List<CyclePeriodRecord> records, List<CycleDailyLog> logs,
                                 CyclePredictionResult prediction, LocalDate today) {
        if (profile == null || !Boolean.TRUE.equals(profile.getReminderEnabled())) {
            return;
        }
        Long userId = profile.getUserId();
        Long loverSpaceId = profile.getDefaultLoverSpaceId();
        boolean shareWithPartner = canNotifyPartner(profile.getShareLevel());
        if (prediction != null && prediction.getPredictedNextStartDate() != null) {
            long daysToNext = ChronoUnit.DAYS.between(today, prediction.getPredictedNextStartDate());
            if (daysToNext == 2) {
                createWarning(userId, loverSpaceId, "PERIOD_COMING_SOON", today, "LOW",
                        "预计生理期快到了",
                        "预计生理期快到了，可以提前准备一下，也给自己留一点更从容的节奏。",
                        shareWithPartner);
            }
            if (daysToNext == 0) {
                createWarning(userId, loverSpaceId, "PERIOD_EXPECTED_TODAY", today, "LOW",
                        "今天可能需要多照顾自己一点",
                        "今天可能是预计生理期开始日。预测不一定准确，可以结合身体感受继续观察。",
                        shareWithPartner);
            }
            if (daysToNext <= -7 && noPeriodAfter(records, prediction.getPredictedNextStartDate())) {
                createWarning(userId, loverSpaceId, "PERIOD_LATE", today, "MEDIUM",
                        "这次周期可能有些推迟",
                        "本次周期可能有些推迟。如果近期压力、作息、饮食变化较大，可能会影响周期；如果持续异常，建议咨询医生。",
                        shareWithPartner);
            }
        }
        for (CyclePeriodRecord record : records) {
            if (record.getStartDate() != null && isLongPeriod(record, today)) {
                createWarning(userId, record.getLoverSpaceId(), "LONG_PERIOD", today, "HIGH",
                        "这次出血持续时间较长",
                        "这次出血持续时间较长，建议关注身体状态；如持续不适或出血较多，建议咨询医生。",
                        shareWithPartner);
            }
        }
        for (CycleDailyLog log : logs) {
            if (FLOW_VERY_HEAVY.equals(log.getFlowLevel())) {
                createWarning(userId, log.getLoverSpaceId(), "VERY_HEAVY_FLOW", log.getLogDate(), "HIGH",
                        "今天记录的经量偏多",
                        "如果出血量明显多于平时，或短时间内频繁更换卫生用品，建议及时咨询医生。",
                        shareWithPartner);
            }
            if (log.getPainLevel() != null && log.getPainLevel() >= 8) {
                createWarning(userId, log.getLoverSpaceId(), "SEVERE_PAIN", log.getLogDate(), "HIGH",
                        "今天记录的疼痛程度较高",
                        "疼痛程度较高，建议休息、保暖；如果疼痛剧烈或反复出现，建议咨询医生。",
                        shareWithPartner);
            }
            if (log.getSleepHours() != null && log.getSleepHours() < 6
                    && log.getPainLevel() != null && log.getPainLevel() >= 5) {
                createWarning(userId, log.getLoverSpaceId(), "LOW_SLEEP_WITH_PAIN", log.getLogDate(), "MEDIUM",
                        "睡眠偏少且疼痛较明显",
                        "睡眠偏少并伴随疼痛，建议今天减少消耗、优先休息；如持续不适可咨询医生。",
                        shareWithPartner);
            }
            if (isBleedingBetweenPeriods(profile, records, log)) {
                createWarning(userId, log.getLoverSpaceId(), "BLEEDING_BETWEEN_PERIODS", log.getLogDate(), "MEDIUM",
                        "出现非经期出血记录",
                        "出现非经期出血，建议关注是否持续；如反复出现，建议咨询医生。",
                        shareWithPartner);
            }
            if (hasFeverOrSick(log)) {
                createWarning(userId, log.getLoverSpaceId(), "FEVER_OR_SICK", log.getLogDate(), "HIGH",
                        "记录到发热或明显不适",
                        "如果发热并伴随明显不适，请及时寻求专业帮助。",
                        shareWithPartner);
            }
        }
        createIrregularCycleWarnings(userId, loverSpaceId, records, shareWithPartner);
        createContinuedMoodWarning(userId, loverSpaceId, logs, shareWithPartner);
    }

    public List<CycleWarning> listActiveWarnings(Long userId) {
        return warningMapper.selectList(new LambdaQueryWrapper<CycleWarning>()
                .eq(CycleWarning::getUserId, userId)
                .eq(CycleWarning::getStatus, ACTIVE_STATUS)
                .orderByDesc(CycleWarning::getWarningDate)
                .orderByDesc(CycleWarning::getCreatedAt));
    }

    public CycleWarning requireOwnActiveWarning(Long warningId, Long userId) {
        CycleWarning warning = warningMapper.selectById(warningId);
        if (warning == null || !userId.equals(warning.getUserId()) || !ACTIVE_STATUS.equals(warning.getStatus())) {
            return null;
        }
        return warning;
    }

    public void dismissWarning(CycleWarning warning) {
        warning.setStatus(DISMISSED_STATUS);
        warning.setUpdatedAt(LocalDateTime.now());
        warningMapper.updateById(warning);
    }

    private void createIrregularCycleWarnings(Long userId, Long loverSpaceId, List<CyclePeriodRecord> records, boolean shareWithPartner) {
        for (int i = 0; i + 1 < records.size(); i++) {
            CyclePeriodRecord current = records.get(i);
            CyclePeriodRecord previous = records.get(i + 1);
            if (current.getStartDate() == null || previous.getStartDate() == null) {
                continue;
            }
            long length = ChronoUnit.DAYS.between(previous.getStartDate(), current.getStartDate());
            if (length > 0 && (length < 21 || length > 35)) {
                createWarning(userId, current.getLoverSpaceId() == null ? loverSpaceId : current.getLoverSpaceId(), "IRREGULAR_CYCLE", current.getStartDate(), "MEDIUM",
                        "本次周期与常见范围有差异",
                        "本次周期与常见范围有差异，建议继续记录；如果多次出现明显不规律，建议咨询医生。",
                        shareWithPartner);
                return;
            }
        }
    }

    private boolean noPeriodAfter(List<CyclePeriodRecord> records, LocalDate expectedStart) {
        for (CyclePeriodRecord record : records) {
            if (record.getStartDate() != null && !record.getStartDate().isBefore(expectedStart)) {
                return false;
            }
        }
        return true;
    }

    private boolean isLongPeriod(CyclePeriodRecord record, LocalDate today) {
        LocalDate end = record.getEndDate() == null ? today : record.getEndDate();
        return ChronoUnit.DAYS.between(record.getStartDate(), end) + 1 > 7;
    }

    private boolean isBleedingBetweenPeriods(CycleCareProfile profile, List<CyclePeriodRecord> records, CycleDailyLog log) {
        if (log.getLogDate() == null || !StringUtils.hasText(log.getFlowLevel()) || FLOW_NONE.equals(log.getFlowLevel())) {
            return false;
        }
        return !predictionService.isPeriodDate(profile, records, log.getLogDate());
    }

    private boolean hasFeverOrSick(CycleDailyLog log) {
        String symptoms = log.getSymptoms();
        return StringUtils.hasText(symptoms)
                && (symptoms.toLowerCase().contains("fever") || symptoms.toLowerCase().contains("sick"));
    }

    private void createWarning(Long userId, Long loverSpaceId, String type, LocalDate date, String severity, String title, String message, boolean shareWithPartner) {
        if (exists(userId, type, date)) {
            return;
        }
        CycleWarning warning = new CycleWarning();
        warning.setUserId(userId);
        warning.setLoverSpaceId(loverSpaceId);
        warning.setWarningType(type);
        warning.setWarningDate(date);
        warning.setSeverity(severity);
        warning.setTitle(title);
        warning.setMessage(message);
        warning.setStatus(ACTIVE_STATUS);
        warning.setCreatedAt(LocalDateTime.now());
        warning.setUpdatedAt(LocalDateTime.now());
        warningMapper.insert(warning);
        notificationService.createNotification(
                userId,
                null,
                notificationType(type),
                title,
                message,
                "CYCLE_WARNING",
                warning.getId(),
                loverSpaceId,
                Map.of("warningType", type, "warningDate", date.toString())
        );
        createPartnerCareNotificationIfAllowed(warning, shareWithPartner);
    }

    private String notificationType(String warningType) {
        if ("PERIOD_COMING_SOON".equals(warningType)) {
            return "CYCLE_PERIOD_COMING";
        }
        if ("PERIOD_EXPECTED_TODAY".equals(warningType)) {
            return "CYCLE_PERIOD_EXPECTED_TODAY";
        }
        if ("PERIOD_LATE".equals(warningType)) {
            return "CYCLE_PERIOD_LATE";
        }
        if ("SEVERE_PAIN".equals(warningType)) {
            return "CYCLE_WARNING_SEVERE_PAIN";
        }
        if ("VERY_HEAVY_FLOW".equals(warningType)) {
            return "CYCLE_WARNING_HEAVY_FLOW";
        }
        if ("LONG_PERIOD".equals(warningType)) {
            return "CYCLE_WARNING_LONG_PERIOD";
        }
        if ("MOOD_STRESS_CONTINUED".equals(warningType)) {
            return "CYCLE_MOOD_SUPPORT_TIP";
        }
        return "CYCLE_WARNING";
    }

    private boolean exists(Long userId, String type, LocalDate date) {
        Long count = warningMapper.selectCount(new LambdaQueryWrapper<CycleWarning>()
                .eq(CycleWarning::getUserId, userId)
                .eq(CycleWarning::getWarningType, type)
                .eq(CycleWarning::getWarningDate, date));
        return count != null && count > 0;
    }

    private void createContinuedMoodWarning(Long userId, Long loverSpaceId, List<CycleDailyLog> logs, boolean shareWithPartner) {
        if (logs == null || logs.size() < 3) {
            return;
        }
        List<CycleDailyLog> sorted = new ArrayList<CycleDailyLog>(logs);
        sorted.sort(Comparator.comparing(CycleDailyLog::getLogDate, Comparator.nullsLast(Comparator.reverseOrder())));
        int streak = 0;
        LocalDate latestStressDate = null;
        LocalDate previousStressDate = null;
        for (CycleDailyLog log : sorted) {
            if (log.getLogDate() == null) {
                continue;
            }
            if (isStressMood(log.getMood())) {
                if (previousStressDate != null && !previousStressDate.minusDays(1).equals(log.getLogDate())) {
                    streak = 0;
                    latestStressDate = null;
                }
                streak++;
                if (latestStressDate == null) {
                    latestStressDate = log.getLogDate();
                }
                previousStressDate = log.getLogDate();
                if (streak >= 3) {
                    createWarning(userId, log.getLoverSpaceId() == null ? loverSpaceId : log.getLoverSpaceId(),
                            "MOOD_STRESS_CONTINUED", latestStressDate, "MEDIUM",
                            "连续多天情绪压力偏高",
                            "连续多天记录到焦虑、烦躁或压力感。可以先放慢节奏，也可以和心理老师聊聊；本提醒不替代专业心理咨询。",
                            shareWithPartner);
                    return;
                }
            } else {
                streak = 0;
                latestStressDate = null;
                previousStressDate = null;
            }
        }
    }

    private boolean isStressMood(String mood) {
        if (!StringUtils.hasText(mood)) {
            return false;
        }
        String value = mood.trim().toUpperCase();
        return "ANXIOUS".equals(value) || "IRRITABLE".equals(value) || "STRESSED".equals(value) || "SAD".equals(value);
    }

    private void createPartnerCareNotificationIfAllowed(CycleWarning warning, boolean shareWithPartner) {
        if (!shareWithPartner || warning.getLoverSpaceId() == null) {
            return;
        }
        List<RelationshipMember> members = relationshipPermissionService.listActiveMembers(warning.getLoverSpaceId());
        for (RelationshipMember member : members) {
            if (!warning.getUserId().equals(member.getUserId())) {
                notificationService.createNotification(
                        member.getUserId(),
                        warning.getUserId(),
                        "CYCLE_PARTNER_CARE_TIP",
                        "今天可能需要更多关心",
                        "对方今天可能需要更多关心，可以主动分担一些事情。",
                        "CYCLE_CARE",
                        warning.getId(),
                        warning.getLoverSpaceId(),
                        Map.of("warningType", warning.getWarningType())
                );
            }
        }
    }

    private boolean canNotifyPartner(String shareLevel) {
        String value = normalizeShareLevel(shareLevel);
        return SUMMARY_SHARE.equals(value) || FULL_SHARE.equals(value);
    }

    private String normalizeShareLevel(String shareLevel) {
        if (!StringUtils.hasText(shareLevel)) {
            return PRIVATE_SHARE;
        }
        String value = shareLevel.trim().toUpperCase();
        if (BASIC_SHARE.equals(value)) {
            return SUMMARY_SHARE;
        }
        if (DETAILED_SHARE.equals(value)) {
            return FULL_SHARE;
        }
        return value;
    }

}
