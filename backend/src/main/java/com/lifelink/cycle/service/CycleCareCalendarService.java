package com.lifelink.cycle.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifelink.common.BusinessException;
import com.lifelink.cycle.dto.CycleCalendarEventResponse;
import com.lifelink.cycle.entity.CycleCareProfile;
import com.lifelink.cycle.entity.CycleDailyAdviceReport;
import com.lifelink.cycle.entity.CyclePeriodRecord;
import com.lifelink.cycle.entity.CycleWarning;
import com.lifelink.cycle.mapper.CycleCareProfileMapper;
import com.lifelink.cycle.mapper.CycleDailyAdviceReportMapper;
import com.lifelink.cycle.mapper.CyclePeriodRecordMapper;
import com.lifelink.cycle.mapper.CycleWarningMapper;
import com.lifelink.relationship.entity.RelationshipMember;
import com.lifelink.relationship.service.RelationshipPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CycleCareCalendarService {

    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String GENERATED_STATUS = "GENERATED";
    private static final String PRIVATE_SHARE = "PRIVATE";
    private static final String SUMMARY_SHARE = "SUMMARY";
    private static final String CALENDAR_ONLY_SHARE = "CALENDAR_ONLY";
    private static final String FULL_SHARE = "FULL";
    private static final String BASIC_SHARE = "BASIC";
    private static final String DETAILED_SHARE = "DETAILED";
    private static final String ESTIMATE_DISCLAIMER = "仅为周期估算，不作为避孕、备孕或医学依据。";

    private final CycleCareAccessService accessService;
    private final RelationshipPermissionService relationshipPermissionService;
    private final CycleCareProfileMapper profileMapper;
    private final CyclePeriodRecordMapper periodRecordMapper;
    private final CycleWarningMapper warningMapper;
    private final CycleDailyAdviceReportMapper reportMapper;
    private final CycleCarePredictionService predictionService;

    public List<CycleCalendarEventResponse> getOwnerCalendarEvents(Long loverSpaceId, Integer year, Integer month, Long userId) {
        CycleCareProfile profile = findProfile(userId);
        Long resolvedSpaceId = accessService.resolveLoverSpaceId(
                userId,
                loverSpaceId == null && profile != null ? profile.getDefaultLoverSpaceId() : loverSpaceId
        );
        YearMonth yearMonth = resolveYearMonth(year, month);
        if (profile == null) {
            return new ArrayList<CycleCalendarEventResponse>();
        }
        return buildFullEvents(profile, resolvedSpaceId, yearMonth, true);
    }

    public List<CycleCalendarEventResponse> getPartnerCalendarEvents(Long spaceId, Integer year, Integer month, Long viewerUserId) {
        accessService.requireActiveLoverSpaceMember(spaceId, viewerUserId);
        YearMonth yearMonth = resolveYearMonth(year, month);
        return buildPartnerEvents(spaceId, viewerUserId, yearMonth);
    }

    public List<CycleCalendarEventResponse> getRelationshipCalendarEvents(Long relationshipId, YearMonth yearMonth, Long viewerUserId) {
        accessService.requireActiveLoverSpaceMember(relationshipId, viewerUserId);
        List<CycleCalendarEventResponse> events = new ArrayList<CycleCalendarEventResponse>();
        CycleCareProfile ownProfile = findProfile(viewerUserId);
        if (ownProfile != null) {
            events.addAll(buildFullEvents(ownProfile, relationshipId, yearMonth, true));
        }
        events.addAll(buildPartnerEvents(relationshipId, viewerUserId, yearMonth));
        return events;
    }

    private List<CycleCalendarEventResponse> buildPartnerEvents(Long spaceId, Long viewerUserId, YearMonth yearMonth) {
        Set<Long> activeUserIds = activeUserIds(spaceId);
        List<CycleCareProfile> profiles = profileMapper.selectList(new LambdaQueryWrapper<CycleCareProfile>()
                .eq(CycleCareProfile::getDefaultLoverSpaceId, spaceId)
                .ne(CycleCareProfile::getUserId, viewerUserId)
                .orderByAsc(CycleCareProfile::getId));
        List<CycleCalendarEventResponse> events = new ArrayList<CycleCalendarEventResponse>();
        for (CycleCareProfile profile : profiles) {
            if (!activeUserIds.contains(profile.getUserId())) {
                continue;
            }
            String shareLevel = normalizeShareLevel(profile.getShareLevel());
            if (PRIVATE_SHARE.equals(shareLevel)) {
                continue;
            }
            if (FULL_SHARE.equals(shareLevel)) {
                events.addAll(buildFullEvents(profile, spaceId, yearMonth, false));
            } else {
                events.addAll(buildCareDayEvents(profile, spaceId, yearMonth, shareLevel));
            }
        }
        return events;
    }

    private List<CycleCalendarEventResponse> buildFullEvents(CycleCareProfile profile, Long spaceId, YearMonth yearMonth, boolean ownerView) {
        List<CyclePeriodRecord> records = listActiveRecords(profile.getUserId(), spaceId);
        List<CycleCalendarEventResponse> events = new ArrayList<CycleCalendarEventResponse>();
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        for (CyclePeriodRecord record : records) {
            LocalDate recordEnd = record.getEndDate() == null ? record.getStartDate() : record.getEndDate();
            if (overlaps(record.getStartDate(), recordEnd, start, end)) {
                addRangeEvents(events, record.getId(), "CYCLE_PERIOD_ACTUAL", ownerView ? "实际生理期" : "周期关怀",
                        maxDate(record.getStartDate(), start), minDate(recordEnd, end),
                        false, spaceId, metadata("endDate", recordEnd == null ? null : recordEnd.toString(), "ownerView", ownerView));
            }
        }

        CyclePredictionResult prediction = predictionService.predict(profile, records, LocalDate.now());
        LocalDate predictedStart = prediction.getPredictedNextStartDate();
        LocalDate predictedEnd = prediction.getPredictedNextEndDate();
        if (predictedStart != null) {
            LocalDate safePredictedEnd = predictedEnd == null ? predictedStart : predictedEnd;
            if (overlaps(predictedStart, safePredictedEnd, start, end)) {
                addRangeEvents(events, null, "CYCLE_PERIOD_PREDICTED", ownerView ? "预计生理期" : "预计关怀日",
                        maxDate(predictedStart, start), minDate(safePredictedEnd, end),
                        true, spaceId, metadata("disclaimer", ESTIMATE_DISCLAIMER, "endDate", predictedEnd == null ? null : predictedEnd.toString()));
            }
            LocalDate ovulation = predictedStart.minusDays(14);
            if (!ovulation.isBefore(start) && !ovulation.isAfter(end)) {
                addDateEvent(events, null, "CYCLE_OVULATION_ESTIMATED", ownerView ? "排卵期估算" : "关怀日",
                        ovulation, true, spaceId, metadata("disclaimer", ESTIMATE_DISCLAIMER));
            }
            LocalDate fertileStart = ovulation.minusDays(5);
            LocalDate fertileEnd = ovulation.plusDays(1);
            if (overlaps(fertileStart, fertileEnd, start, end)) {
                addRangeEvents(events, null, "CYCLE_FERTILE_WINDOW_ESTIMATED", ownerView ? "易孕期估算" : "关怀日",
                        maxDate(fertileStart, start), minDate(fertileEnd, end), true, spaceId, metadata("disclaimer", ESTIMATE_DISCLAIMER));
            }
        }

        List<CycleWarning> warnings = warningMapper.selectList(new LambdaQueryWrapper<CycleWarning>()
                .eq(CycleWarning::getUserId, profile.getUserId())
                .eq(CycleWarning::getLoverSpaceId, spaceId)
                .eq(CycleWarning::getStatus, ACTIVE_STATUS)
                .ge(CycleWarning::getWarningDate, start)
                .le(CycleWarning::getWarningDate, end));
        for (CycleWarning warning : warnings) {
            addDateEvent(events, warning.getId(), "CYCLE_WARNING", ownerView ? warning.getTitle() : "关怀提醒",
                    warning.getWarningDate(), false, spaceId, metadata("severity", warning.getSeverity(), "warningType", warning.getWarningType()));
        }

        List<CycleDailyAdviceReport> reports = reportMapper.selectList(new LambdaQueryWrapper<CycleDailyAdviceReport>()
                .eq(CycleDailyAdviceReport::getUserId, profile.getUserId())
                .eq(CycleDailyAdviceReport::getLoverSpaceId, spaceId)
                .eq(CycleDailyAdviceReport::getStatus, GENERATED_STATUS)
                .ge(CycleDailyAdviceReport::getReportDate, start)
                .le(CycleDailyAdviceReport::getReportDate, end));
        for (CycleDailyAdviceReport report : reports) {
            addDateEvent(events, report.getId(), "CYCLE_DAILY_REPORT", ownerView ? "周期日报" : "关怀摘要",
                    report.getReportDate(), false, spaceId, metadata("riskLevel", report.getRiskLevel()));
        }
        return events;
    }

    private List<CycleCalendarEventResponse> buildCareDayEvents(CycleCareProfile profile, Long spaceId, YearMonth yearMonth, String shareLevel) {
        Set<LocalDate> dates = new LinkedHashSet<LocalDate>();
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        List<CyclePeriodRecord> records = listActiveRecords(profile.getUserId(), spaceId);
        for (CyclePeriodRecord record : records) {
            LocalDate recordEnd = record.getEndDate() == null ? record.getStartDate() : record.getEndDate();
            collectRange(dates, record.getStartDate(), recordEnd, start, end);
        }
        CyclePredictionResult prediction = predictionService.predict(profile, records, LocalDate.now());
        if (prediction.getPredictedNextStartDate() != null) {
            collectRange(dates, prediction.getPredictedNextStartDate(),
                    prediction.getPredictedNextEndDate() == null ? prediction.getPredictedNextStartDate() : prediction.getPredictedNextEndDate(),
                    start, end);
        }
        for (CycleWarning warning : warningMapper.selectList(new LambdaQueryWrapper<CycleWarning>()
                .eq(CycleWarning::getUserId, profile.getUserId())
                .eq(CycleWarning::getLoverSpaceId, spaceId)
                .eq(CycleWarning::getStatus, ACTIVE_STATUS)
                .ge(CycleWarning::getWarningDate, start)
                .le(CycleWarning::getWarningDate, end))) {
            dates.add(warning.getWarningDate());
        }
        for (CycleDailyAdviceReport report : reportMapper.selectList(new LambdaQueryWrapper<CycleDailyAdviceReport>()
                .eq(CycleDailyAdviceReport::getUserId, profile.getUserId())
                .eq(CycleDailyAdviceReport::getLoverSpaceId, spaceId)
                .eq(CycleDailyAdviceReport::getStatus, GENERATED_STATUS)
                .ge(CycleDailyAdviceReport::getReportDate, start)
                .le(CycleDailyAdviceReport::getReportDate, end))) {
            dates.add(report.getReportDate());
        }

        String title = CALENDAR_ONLY_SHARE.equals(shareLevel) ? "关怀日" : "关怀提醒";
        List<CycleCalendarEventResponse> events = new ArrayList<CycleCalendarEventResponse>();
        for (LocalDate date : dates) {
            addDateEvent(events, null, "CYCLE_CARE_DAY", title, date, false, spaceId, metadata("shareLevel", shareLevel));
        }
        return events;
    }

    private List<CyclePeriodRecord> listActiveRecords(Long userId, Long spaceId) {
        return periodRecordMapper.selectList(new LambdaQueryWrapper<CyclePeriodRecord>()
                .eq(CyclePeriodRecord::getUserId, userId)
                .eq(CyclePeriodRecord::getLoverSpaceId, spaceId)
                .eq(CyclePeriodRecord::getStatus, ACTIVE_STATUS)
                .orderByDesc(CyclePeriodRecord::getStartDate));
    }

    private CycleCareProfile findProfile(Long userId) {
        if (userId == null) {
            return null;
        }
        return profileMapper.selectOne(new LambdaQueryWrapper<CycleCareProfile>()
                .eq(CycleCareProfile::getUserId, userId)
                .last("LIMIT 1"));
    }

    private Set<Long> activeUserIds(Long spaceId) {
        List<RelationshipMember> members = relationshipPermissionService.listActiveMembers(spaceId);
        Set<Long> ids = new HashSet<Long>();
        for (RelationshipMember member : members) {
            ids.add(member.getUserId());
        }
        return ids;
    }

    private YearMonth resolveYearMonth(Integer year, Integer month) {
        try {
            return YearMonth.of(year == null ? YearMonth.now().getYear() : year, month == null ? YearMonth.now().getMonthValue() : month);
        } catch (DateTimeException ex) {
            throw new BusinessException(400, "Invalid year or month");
        }
    }

    private void addRangeEvents(List<CycleCalendarEventResponse> events, Long id, String type, String title,
                                LocalDate start, LocalDate end, boolean predicted, Long spaceId, Map<String, Object> metadata) {
        if (start == null) {
            return;
        }
        LocalDate current = start;
        LocalDate safeEnd = end == null ? start : end;
        while (!current.isAfter(safeEnd)) {
            addDateEvent(events, id, type, title, current, predicted, spaceId, metadata);
            current = current.plusDays(1);
        }
    }

    private void addDateEvent(List<CycleCalendarEventResponse> events, Long id, String type, String title,
                              LocalDate date, boolean predicted, Long spaceId, Map<String, Object> metadata) {
        if (date == null) {
            return;
        }
        events.add(new CycleCalendarEventResponse(id, type, title, date, predicted, spaceId, metadata));
    }

    private void collectRange(Set<LocalDate> dates, LocalDate start, LocalDate end, LocalDate windowStart, LocalDate windowEnd) {
        if (start == null) {
            return;
        }
        LocalDate safeEnd = end == null ? start : end;
        LocalDate current = start.isBefore(windowStart) ? windowStart : start;
        while (!current.isAfter(safeEnd) && !current.isAfter(windowEnd)) {
            dates.add(current);
            current = current.plusDays(1);
        }
    }

    private boolean overlaps(LocalDate start, LocalDate end, LocalDate windowStart, LocalDate windowEnd) {
        if (start == null) {
            return false;
        }
        LocalDate safeEnd = end == null ? start : end;
        return !safeEnd.isBefore(windowStart) && !start.isAfter(windowEnd);
    }

    private LocalDate maxDate(LocalDate a, LocalDate b) {
        if (a == null) {
            return b;
        }
        return a.isAfter(b) ? a : b;
    }

    private LocalDate minDate(LocalDate a, LocalDate b) {
        if (a == null) {
            return b;
        }
        return a.isBefore(b) ? a : b;
    }

    private Map<String, Object> metadata(Object... keyValues) {
        Map<String, Object> result = new HashMap<String, Object>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            Object key = keyValues[i];
            Object value = keyValues[i + 1];
            if (key != null && value != null) {
                result.put(String.valueOf(key), value);
            }
        }
        return result;
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
        if (!SUMMARY_SHARE.equals(value) && !CALENDAR_ONLY_SHARE.equals(value) && !FULL_SHARE.equals(value)) {
            return PRIVATE_SHARE;
        }
        return value;
    }
}
