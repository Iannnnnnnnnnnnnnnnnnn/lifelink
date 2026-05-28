package com.lifelink.cycle.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelink.common.BusinessException;
import com.lifelink.cycle.config.CycleCareDailyAdviceProperties;
import com.lifelink.cycle.dto.CycleDailyAdviceReportResponse;
import com.lifelink.cycle.entity.CycleCareProfile;
import com.lifelink.cycle.entity.CycleDailyAdviceReport;
import com.lifelink.cycle.entity.CycleDailyLog;
import com.lifelink.cycle.entity.CyclePeriodRecord;
import com.lifelink.cycle.entity.CycleWarning;
import com.lifelink.cycle.mapper.CycleCareProfileMapper;
import com.lifelink.cycle.mapper.CycleDailyAdviceReportMapper;
import com.lifelink.cycle.mapper.CycleDailyLogMapper;
import com.lifelink.cycle.mapper.CyclePeriodRecordMapper;
import com.lifelink.cycle.mapper.CycleWarningMapper;
import com.lifelink.notification.service.NotificationService;
import com.lifelink.relationship.entity.RelationshipMember;
import com.lifelink.relationship.service.RelationshipPermissionService;
import com.lifelink.user.entity.User;
import com.lifelink.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CycleCareDailyAdviceService {

    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String GENERATED_STATUS = "GENERATED";
    private static final String FAILED_STATUS = "FAILED";
    private static final String PRIVATE_SHARE = "PRIVATE";
    private static final String CALENDAR_ONLY_SHARE = "CALENDAR_ONLY";
    private static final String BASIC_SHARE = "BASIC";
    private static final String SUMMARY_SHARE = "SUMMARY";
    private static final String DETAILED_SHARE = "DETAILED";
    private static final String FULL_SHARE = "FULL";
    private static final String DEFAULT_TIMEZONE = "Asia/Shanghai";

    private final CycleCareProfileMapper profileMapper;
    private final CycleDailyAdviceReportMapper reportMapper;
    private final CyclePeriodRecordMapper periodRecordMapper;
    private final CycleDailyLogMapper dailyLogMapper;
    private final CycleWarningMapper warningMapper;
    private final UserMapper userMapper;
    private final CycleCareAccessService accessService;
    private final CycleCarePredictionService predictionService;
    private final CycleCareWarningService warningService;
    private final CycleCareDailyAdviceGenerator generator;
    private final CycleCareDailyAdviceProperties properties;
    private final NotificationService notificationService;
    private final RelationshipPermissionService relationshipPermissionService;
    private final ObjectMapper objectMapper;

    public CycleCareDailyAdviceJobSummary runDailyJob() {
        CycleCareDailyAdviceJobSummary summary = new CycleCareDailyAdviceJobSummary();
        List<CycleCareProfile> profiles = profileMapper.selectList(new LambdaQueryWrapper<CycleCareProfile>()
                .orderByAsc(CycleCareProfile::getId));
        for (CycleCareProfile profile : profiles) {
            summary.incrementTotal();
            try {
                GenerationResult result = generateScheduled(profile);
                if (GenerationResult.SUCCESS.equals(result)) {
                    summary.incrementSuccess();
                } else {
                    summary.incrementSkipped();
                }
            } catch (Exception ex) {
                saveFailureQuietly(profile, ex);
                summary.incrementFailed();
            }
        }
        return summary;
    }

    public CycleDailyAdviceReportResponse getLatestReport(Long userId) {
        accessService.requireAccess(userId);
        CycleDailyAdviceReport report = reportMapper.selectOne(new LambdaQueryWrapper<CycleDailyAdviceReport>()
                .eq(CycleDailyAdviceReport::getUserId, userId)
                .eq(CycleDailyAdviceReport::getStatus, GENERATED_STATUS)
                .orderByDesc(CycleDailyAdviceReport::getReportDate)
                .orderByDesc(CycleDailyAdviceReport::getCreatedAt)
                .last("LIMIT 1"));
        return report == null ? null : toResponse(report, false, true);
    }

    public List<CycleDailyAdviceReportResponse> listReports(LocalDate startDate, LocalDate endDate, Long userId) {
        accessService.requireAccess(userId);
        LocalDate end = endDate == null ? LocalDate.now() : endDate;
        LocalDate start = startDate == null ? end.minusDays(30) : startDate;
        if (start.isAfter(end)) {
            throw new BusinessException(400, "Start date cannot be after end date");
        }
        List<CycleDailyAdviceReport> reports = reportMapper.selectList(new LambdaQueryWrapper<CycleDailyAdviceReport>()
                .eq(CycleDailyAdviceReport::getUserId, userId)
                .eq(CycleDailyAdviceReport::getStatus, GENERATED_STATUS)
                .ge(CycleDailyAdviceReport::getReportDate, start)
                .le(CycleDailyAdviceReport::getReportDate, end)
                .orderByDesc(CycleDailyAdviceReport::getReportDate));
        List<CycleDailyAdviceReportResponse> responses = new ArrayList<CycleDailyAdviceReportResponse>();
        for (CycleDailyAdviceReport report : reports) {
            responses.add(toResponse(report, false, true));
        }
        return responses;
    }

    public CycleDailyAdviceReportResponse getReport(LocalDate date, Long userId) {
        accessService.requireAccess(userId);
        if (date == null) {
            throw new BusinessException(400, "Date is required");
        }
        CycleDailyAdviceReport report = findReport(userId, date);
        if (report == null || !GENERATED_STATUS.equals(report.getStatus())) {
            return null;
        }
        return toResponse(report, false, true);
    }

    @Transactional
    public CycleDailyAdviceReportResponse regenerate(LocalDate date, Long userId) {
        if (date == null) {
            throw new BusinessException(400, "Date is required");
        }
        CycleCareProfile profile = requireProfile(userId);
        Long loverSpaceId = resolveActiveLoverSpaceId(profile);
        ZoneId zoneId = resolveZone(profile.getTimezone());
        LocalDate today = LocalDate.now(zoneId);
        if (date.isAfter(today)) {
            throw new BusinessException(400, "Cannot regenerate a future report");
        }
        int maxDays = properties.getMaxRegenerateDays() == null ? 30 : properties.getMaxRegenerateDays();
        if (date.isBefore(today.minusDays(maxDays))) {
            throw new BusinessException(400, "Report date is outside the regeneration window");
        }
        CycleDailyAdviceReport report = generateAndSave(profile, loverSpaceId, date, true, false);
        return toResponse(report, false, true);
    }

    public CycleDailyAdviceReportResponse getLatestPartnerReport(Long loverSpaceId, Long userId) {
        relationshipPermissionService.requireActiveRelationshipMember(loverSpaceId, userId);
        List<RelationshipMember> members = relationshipPermissionService.listActiveMembers(loverSpaceId);
        Set<Long> activeUserIds = new HashSet<Long>();
        for (RelationshipMember member : members) {
            activeUserIds.add(member.getUserId());
        }
        List<CycleDailyAdviceReport> reports = reportMapper.selectList(new LambdaQueryWrapper<CycleDailyAdviceReport>()
                .eq(CycleDailyAdviceReport::getLoverSpaceId, loverSpaceId)
                .eq(CycleDailyAdviceReport::getStatus, GENERATED_STATUS)
                .orderByDesc(CycleDailyAdviceReport::getReportDate)
                .orderByDesc(CycleDailyAdviceReport::getCreatedAt));
        for (CycleDailyAdviceReport report : reports) {
            if (userId.equals(report.getUserId()) || !activeUserIds.contains(report.getUserId())) {
                continue;
            }
            CycleCareProfile ownerProfile = findProfile(report.getUserId());
            if (ownerProfile == null || !partnerCanView(ownerProfile.getShareLevel())) {
                continue;
            }
            return toResponse(report, true, partnerFullAllowed(ownerProfile.getShareLevel()));
        }
        return null;
    }

    private GenerationResult generateScheduled(CycleCareProfile profile) {
        if (!isActiveUser(profile.getUserId())) {
            return GenerationResult.SKIPPED;
        }
        Long loverSpaceId;
        try {
            loverSpaceId = resolveActiveLoverSpaceId(profile);
        } catch (BusinessException ex) {
            return GenerationResult.SKIPPED;
        }
        LocalDate targetDate = LocalDate.now(resolveZone(profile.getTimezone())).minusDays(1);
        CycleDailyAdviceReport existing = findReport(profile.getUserId(), targetDate);
        if (existing != null && GENERATED_STATUS.equals(existing.getStatus())) {
            return GenerationResult.SKIPPED;
        }
        if (!hasGenerationSignal(profile, loverSpaceId, targetDate)) {
            return GenerationResult.SKIPPED;
        }
        generateAndSave(profile, loverSpaceId, targetDate, false, true);
        return GenerationResult.SUCCESS;
    }

    private CycleDailyAdviceReport generateAndSave(CycleCareProfile profile, Long loverSpaceId, LocalDate targetDate,
                                                   boolean force, boolean notify) {
        CycleDailyAdviceReport existing = findReport(profile.getUserId(), targetDate);
        if (!force && existing != null && GENERATED_STATUS.equals(existing.getStatus())) {
            return existing;
        }

        CycleDailyLog log = findLog(profile.getUserId(), targetDate);
        List<CyclePeriodRecord> records = listActiveRecords(profile.getUserId(), loverSpaceId);
        CycleCareProfile runtimeProfile = copyProfile(profile, loverSpaceId);
        CyclePredictionResult prediction = predictionService.predict(runtimeProfile, records, targetDate);
        List<CycleDailyLog> nearbyLogs = listLogs(profile.getUserId(), loverSpaceId, targetDate.minusDays(45), targetDate);
        warningService.generateWarnings(runtimeProfile, records, nearbyLogs, prediction, targetDate);
        List<CycleWarning> warnings = listWarnings(profile.getUserId(), targetDate);
        boolean hasSourceData = hasSourceData(log, records, warnings, targetDate);

        CycleDailyAdviceDraft draft = generator.generate(runtimeProfile, targetDate, log, records, prediction, warnings, hasSourceData);
        CycleDailyAdviceReport report = existing == null ? new CycleDailyAdviceReport() : existing;
        copyDraftToReport(report, draft, profile.getUserId(), loverSpaceId);
        if (report.getId() == null) {
            reportMapper.insert(report);
        } else {
            reportMapper.updateById(report);
        }
        if (notify) {
            try {
                createNotifications(report);
            } catch (Exception ignored) {
                // Notification delivery must not turn a generated report into a failed job item.
            }
        }
        return report;
    }

    private boolean hasGenerationSignal(CycleCareProfile profile, Long loverSpaceId, LocalDate targetDate) {
        CycleDailyLog log = findLog(profile.getUserId(), targetDate);
        List<CyclePeriodRecord> records = listActiveRecords(profile.getUserId(), loverSpaceId);
        CycleCareProfile runtimeProfile = copyProfile(profile, loverSpaceId);
        CyclePredictionResult prediction = predictionService.predict(runtimeProfile, records, targetDate);
        List<CycleDailyLog> nearbyLogs = listLogs(profile.getUserId(), loverSpaceId, targetDate.minusDays(45), targetDate);
        warningService.generateWarnings(runtimeProfile, records, nearbyLogs, prediction, targetDate);
        List<CycleWarning> warnings = listWarnings(profile.getUserId(), targetDate);
        return hasSourceData(log, records, warnings, targetDate)
                || Boolean.TRUE.equals(prediction.getPredictedPeriod())
                || (CycleCarePredictionService.LUTEAL.equals(prediction.getPhase()) && prediction.getDaysToNextPeriod() != null && prediction.getDaysToNextPeriod() >= 0 && prediction.getDaysToNextPeriod() <= 7)
                || Boolean.TRUE.equals(profile.getReminderEnabled());
    }

    private boolean hasSourceData(CycleDailyLog log, List<CyclePeriodRecord> records, List<CycleWarning> warnings, LocalDate targetDate) {
        return log != null || coversDate(records, targetDate) || (warnings != null && !warnings.isEmpty());
    }

    private boolean coversDate(List<CyclePeriodRecord> records, LocalDate targetDate) {
        if (records == null) {
            return false;
        }
        for (CyclePeriodRecord record : records) {
            LocalDate start = record.getStartDate();
            LocalDate end = record.getEndDate() == null ? targetDate : record.getEndDate();
            if (start != null && !targetDate.isBefore(start) && !targetDate.isAfter(end)) {
                return true;
            }
        }
        return false;
    }

    private void copyDraftToReport(CycleDailyAdviceReport report, CycleDailyAdviceDraft draft, Long userId, Long loverSpaceId) {
        LocalDateTime now = LocalDateTime.now();
        report.setUserId(userId);
        report.setLoverSpaceId(loverSpaceId);
        report.setReportDate(draft.getReportDate());
        report.setPhase(draft.getPhase());
        report.setPhaseLabel(draft.getPhaseLabel());
        report.setPredictedPhase(draft.getPredictedPhase());
        report.setSummary(draft.getSummary());
        report.setBodyStatusSummary(draft.getBodyStatusSummary());
        report.setFlowSummary(draft.getFlowSummary());
        report.setPainSummary(draft.getPainSummary());
        report.setMoodSummary(draft.getMoodSummary());
        report.setSymptomSummary(draft.getSymptomSummary());
        report.setClothingAdvice(draft.getClothingAdvice());
        report.setFoodAdvice(draft.getFoodAdvice());
        report.setRestAdvice(draft.getRestAdvice());
        report.setMoodAdvice(draft.getMoodAdvice());
        report.setPartnerAdvice(draft.getPartnerAdvice());
        report.setWarningSummary(draft.getWarningSummary());
        report.setRiskLevel(draft.getRiskLevel());
        report.setWarningTypes(writeJson(new ArrayList<String>(draft.getWarningTypes())));
        report.setShareLevelSnapshot(draft.getShareLevelSnapshot());
        report.setPartnerVisibleSummary(draft.getPartnerVisibleSummary());
        report.setSourceType(draft.getSourceType());
        report.setAiGenerated(draft.getAiGenerated());
        report.setAiModel(draft.getAiModel());
        report.setPromptVersion(draft.getPromptVersion());
        report.setRawAiResponse(draft.getRawAiResponse());
        report.setStatus(GENERATED_STATUS);
        report.setErrorMessage(null);
        if (report.getCreatedAt() == null) {
            report.setCreatedAt(now);
        }
        report.setUpdatedAt(now);
    }

    private void createNotifications(CycleDailyAdviceReport report) {
        notificationService.createNotification(
                report.getUserId(),
                null,
                "CYCLE_DAILY_ADVICE_READY",
                "今日关怀建议已生成",
                "根据你昨天的记录，系统为你整理了一份关怀建议，可以查看今天如何更好照顾自己。",
                "CYCLE_DAILY_ADVICE_REPORT",
                report.getId(),
                report.getLoverSpaceId(),
                Map.of("reportDate", report.getReportDate().toString())
        );
        List<String> warningTypes = readWarningTypes(report.getWarningTypes());
        if (!warningTypes.isEmpty()) {
            notificationService.createNotification(
                    report.getUserId(),
                    null,
                    warningNotificationType(warningTypes),
                    "有一条身体状态提醒",
                    "昨天的记录显示有需要关注的地方。建议继续观察，如持续不适可咨询医生。",
                    "CYCLE_DAILY_ADVICE_REPORT",
                    report.getId(),
                    report.getLoverSpaceId(),
                    Map.of("reportDate", report.getReportDate().toString(), "warningTypes", warningTypes)
            );
        }
        createPartnerNotificationIfAllowed(report);
    }

    private void createPartnerNotificationIfAllowed(CycleDailyAdviceReport report) {
        if (report.getLoverSpaceId() == null || !partnerCanView(report.getShareLevelSnapshot())) {
            return;
        }
        List<RelationshipMember> members = relationshipPermissionService.listActiveMembers(report.getLoverSpaceId());
        for (RelationshipMember member : members) {
            if (report.getUserId().equals(member.getUserId())) {
                continue;
            }
            notificationService.createNotification(
                    member.getUserId(),
                    report.getUserId(),
                    "CYCLE_PARTNER_CARE_TIP",
                    "今日关怀提醒",
                    "对方今天可能需要更多关心，可以主动分担一些事情，温柔表达关心。",
                    "CYCLE_DAILY_ADVICE_REPORT",
                    report.getId(),
                    report.getLoverSpaceId(),
                    Map.of("reportDate", report.getReportDate().toString())
            );
        }
    }

    private String warningNotificationType(List<String> warningTypes) {
        if (warningTypes.contains("SEVERE_PAIN")) {
            return "CYCLE_WARNING_SEVERE_PAIN";
        }
        if (warningTypes.contains("VERY_HEAVY_FLOW")) {
            return "CYCLE_WARNING_HEAVY_FLOW";
        }
        if (warningTypes.contains("LONG_PERIOD")) {
            return "CYCLE_WARNING_LONG_PERIOD";
        }
        return "CYCLE_WARNING";
    }

    private CycleDailyAdviceReportResponse toResponse(CycleDailyAdviceReport report, boolean partnerView, boolean fullAllowed) {
        List<String> warningTypes = readWarningTypes(report.getWarningTypes());
        if (!partnerView || fullAllowed) {
            return new CycleDailyAdviceReportResponse(
                    report.getId(),
                    report.getLoverSpaceId(),
                    report.getReportDate(),
                    report.getPhase(),
                    report.getPhaseLabel(),
                    report.getPredictedPhase(),
                    partnerView ? report.getPartnerVisibleSummary() : report.getSummary(),
                    report.getBodyStatusSummary(),
                    report.getFlowSummary(),
                    report.getPainSummary(),
                    report.getMoodSummary(),
                    report.getSymptomSummary(),
                    report.getClothingAdvice(),
                    report.getFoodAdvice(),
                    report.getRestAdvice(),
                    report.getMoodAdvice(),
                    report.getPartnerAdvice(),
                    report.getWarningSummary(),
                    report.getRiskLevel(),
                    warningTypes,
                    report.getShareLevelSnapshot(),
                    report.getPartnerVisibleSummary(),
                    report.getSourceType(),
                    report.getAiGenerated(),
                    report.getStatus(),
                    CycleCareAdviceService.DISCLAIMER,
                    partnerView,
                    report.getCreatedAt(),
                    report.getUpdatedAt()
            );
        }
        return new CycleDailyAdviceReportResponse(
                report.getId(),
                report.getLoverSpaceId(),
                report.getReportDate(),
                report.getPhase(),
                report.getPhaseLabel(),
                report.getPredictedPhase(),
                report.getPartnerVisibleSummary(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                report.getPartnerVisibleSummary(),
                null,
                report.getRiskLevel(),
                warningTypes.isEmpty() ? Collections.emptyList() : List.of("CARE_NEEDED"),
                report.getShareLevelSnapshot(),
                report.getPartnerVisibleSummary(),
                report.getSourceType(),
                report.getAiGenerated(),
                report.getStatus(),
                CycleCareAdviceService.DISCLAIMER,
                true,
                report.getCreatedAt(),
                report.getUpdatedAt()
        );
    }

    private Long resolveActiveLoverSpaceId(CycleCareProfile profile) {
        List<Long> spaceIds = accessService.getAccessibleLoverSpaceIds(profile.getUserId());
        if (spaceIds.isEmpty()) {
            throw new BusinessException(403, CycleCareAccessService.ACCESS_DENIED_MESSAGE);
        }
        if (profile.getDefaultLoverSpaceId() != null && spaceIds.contains(profile.getDefaultLoverSpaceId())) {
            return profile.getDefaultLoverSpaceId();
        }
        return spaceIds.get(0);
    }

    private CycleCareProfile requireProfile(Long userId) {
        accessService.requireAccess(userId);
        CycleCareProfile profile = findProfile(userId);
        if (profile == null) {
            throw new BusinessException(404, "Cycle care profile not found");
        }
        return profile;
    }

    private CycleCareProfile findProfile(Long userId) {
        return profileMapper.selectOne(new LambdaQueryWrapper<CycleCareProfile>()
                .eq(CycleCareProfile::getUserId, userId)
                .last("LIMIT 1"));
    }

    private boolean isActiveUser(Long userId) {
        User user = userId == null ? null : userMapper.selectById(userId);
        return user != null && ACTIVE_STATUS.equals(user.getStatus());
    }

    private CycleDailyAdviceReport findReport(Long userId, LocalDate date) {
        return reportMapper.selectOne(new LambdaQueryWrapper<CycleDailyAdviceReport>()
                .eq(CycleDailyAdviceReport::getUserId, userId)
                .eq(CycleDailyAdviceReport::getReportDate, date)
                .last("LIMIT 1"));
    }

    private CycleDailyLog findLog(Long userId, LocalDate date) {
        return dailyLogMapper.selectOne(new LambdaQueryWrapper<CycleDailyLog>()
                .eq(CycleDailyLog::getUserId, userId)
                .eq(CycleDailyLog::getLogDate, date)
                .last("LIMIT 1"));
    }

    private List<CyclePeriodRecord> listActiveRecords(Long userId, Long loverSpaceId) {
        LambdaQueryWrapper<CyclePeriodRecord> wrapper = new LambdaQueryWrapper<CyclePeriodRecord>()
                .eq(CyclePeriodRecord::getUserId, userId)
                .eq(CyclePeriodRecord::getStatus, ACTIVE_STATUS)
                .orderByDesc(CyclePeriodRecord::getStartDate);
        if (loverSpaceId != null) {
            wrapper.eq(CyclePeriodRecord::getLoverSpaceId, loverSpaceId);
        }
        return periodRecordMapper.selectList(wrapper);
    }

    private List<CycleDailyLog> listLogs(Long userId, Long loverSpaceId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<CycleDailyLog> wrapper = new LambdaQueryWrapper<CycleDailyLog>()
                .eq(CycleDailyLog::getUserId, userId)
                .ge(CycleDailyLog::getLogDate, startDate)
                .le(CycleDailyLog::getLogDate, endDate)
                .orderByDesc(CycleDailyLog::getLogDate);
        if (loverSpaceId != null) {
            wrapper.eq(CycleDailyLog::getLoverSpaceId, loverSpaceId);
        }
        return dailyLogMapper.selectList(wrapper);
    }

    private List<CycleWarning> listWarnings(Long userId, LocalDate targetDate) {
        return warningMapper.selectList(new LambdaQueryWrapper<CycleWarning>()
                .eq(CycleWarning::getUserId, userId)
                .eq(CycleWarning::getWarningDate, targetDate)
                .eq(CycleWarning::getStatus, ACTIVE_STATUS)
                .orderByDesc(CycleWarning::getCreatedAt));
    }

    private CycleCareProfile copyProfile(CycleCareProfile source, Long loverSpaceId) {
        CycleCareProfile profile = new CycleCareProfile();
        profile.setId(source.getId());
        profile.setUserId(source.getUserId());
        profile.setDefaultLoverSpaceId(loverSpaceId);
        profile.setCycleLength(source.getCycleLength());
        profile.setPeriodLength(source.getPeriodLength());
        profile.setLastPeriodStartDate(source.getLastPeriodStartDate());
        profile.setReminderEnabled(source.getReminderEnabled());
        profile.setShareLevel(normalizeShareLevel(source.getShareLevel()));
        profile.setTimezone(source.getTimezone());
        profile.setCreatedAt(source.getCreatedAt());
        profile.setUpdatedAt(source.getUpdatedAt());
        return profile;
    }

    private ZoneId resolveZone(String timezone) {
        try {
            return ZoneId.of(StringUtils.hasText(timezone) ? timezone.trim() : properties.getZone());
        } catch (DateTimeException ex) {
            return ZoneId.of(DEFAULT_TIMEZONE);
        }
    }

    private boolean partnerCanView(String shareLevel) {
        String value = normalizeShareLevel(shareLevel);
        return !PRIVATE_SHARE.equals(value) && !CALENDAR_ONLY_SHARE.equals(value);
    }

    private boolean partnerFullAllowed(String shareLevel) {
        String value = normalizeShareLevel(shareLevel);
        return DETAILED_SHARE.equals(value) || FULL_SHARE.equals(value);
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

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            return "[]";
        }
    }

    private List<String> readWarningTypes(String warningTypes) {
        if (!StringUtils.hasText(warningTypes)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(warningTypes, new TypeReference<List<String>>() {
            });
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    private void saveFailureQuietly(CycleCareProfile profile, Exception exception) {
        try {
            if (profile == null || profile.getUserId() == null) {
                return;
            }
            Long loverSpaceId = null;
            try {
                loverSpaceId = resolveActiveLoverSpaceId(profile);
            } catch (Exception ignored) {
                // Keep the failure record user-scoped when the active space is already unavailable.
            }
            LocalDate targetDate = LocalDate.now(resolveZone(profile.getTimezone())).minusDays(1);
            CycleDailyAdviceReport existing = findReport(profile.getUserId(), targetDate);
            if (existing != null && GENERATED_STATUS.equals(existing.getStatus())) {
                return;
            }
            CycleDailyAdviceReport report = existing == null ? new CycleDailyAdviceReport() : existing;
            LocalDateTime now = LocalDateTime.now();
            report.setUserId(profile.getUserId());
            report.setLoverSpaceId(loverSpaceId);
            report.setReportDate(targetDate);
            report.setShareLevelSnapshot(normalizeShareLevel(profile.getShareLevel()));
            report.setSourceType("RULE_BASED");
            report.setAiGenerated(false);
            report.setStatus(FAILED_STATUS);
            report.setErrorMessage(exception.getClass().getSimpleName());
            if (report.getCreatedAt() == null) {
                report.setCreatedAt(now);
            }
            report.setUpdatedAt(now);
            if (report.getId() == null) {
                reportMapper.insert(report);
            } else {
                reportMapper.updateById(report);
            }
        } catch (Exception ignored) {
            // Failure persistence must not stop the batch.
        }
    }

    private enum GenerationResult {
        SUCCESS,
        SKIPPED
    }
}
