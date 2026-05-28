package com.lifelink.cycle.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelink.common.BusinessException;
import com.lifelink.cycle.dto.CreateCyclePeriodRecordRequest;
import com.lifelink.cycle.dto.CycleCalendarEventResponse;
import com.lifelink.cycle.dto.CycleCareAccessResponse;
import com.lifelink.cycle.dto.CycleCareProfileResponse;
import com.lifelink.cycle.dto.CycleDailyLogResponse;
import com.lifelink.cycle.dto.CycleParseLogResponse;
import com.lifelink.cycle.dto.CyclePartnerSummaryResponse;
import com.lifelink.cycle.dto.CyclePeriodRecordResponse;
import com.lifelink.cycle.dto.CycleTodayResponse;
import com.lifelink.cycle.dto.CycleWarningResponse;
import com.lifelink.cycle.dto.UpdateCyclePeriodRecordRequest;
import com.lifelink.cycle.dto.UpdateCycleShareSettingsRequest;
import com.lifelink.cycle.dto.UpsertCycleCareProfileRequest;
import com.lifelink.cycle.dto.UpsertCycleDailyLogRequest;
import com.lifelink.cycle.entity.CycleCareProfile;
import com.lifelink.cycle.entity.CycleDailyLog;
import com.lifelink.cycle.entity.CyclePeriodRecord;
import com.lifelink.cycle.entity.CycleWarning;
import com.lifelink.cycle.mapper.CycleCareProfileMapper;
import com.lifelink.cycle.mapper.CycleDailyLogMapper;
import com.lifelink.cycle.mapper.CyclePeriodRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DateTimeException;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CycleCareService {

    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String DELETED_STATUS = "DELETED";
    private static final String PRIVATE_SHARE = "PRIVATE";
    private static final String BASIC_SHARE = "BASIC";
    private static final String DETAILED_SHARE = "DETAILED";
    private static final String SUMMARY_SHARE = "SUMMARY";
    private static final String CALENDAR_ONLY_SHARE = "CALENDAR_ONLY";
    private static final String FULL_SHARE = "FULL";
    private static final String DEFAULT_TIMEZONE = "Asia/Shanghai";
    private static final String FLOW_NONE = "NONE";

    private final CycleCareAccessService accessService;
    private final CycleCareProfileMapper profileMapper;
    private final CyclePeriodRecordMapper periodRecordMapper;
    private final CycleDailyLogMapper dailyLogMapper;
    private final CycleCarePredictionService predictionService;
    private final CycleCareAdviceService adviceService;
    private final CycleCareWarningService warningService;
    private final ObjectMapper objectMapper;

    public CycleCareAccessResponse getAccess(Long userId) {
        List<Long> loverSpaceIds = accessService.getAccessibleLoverSpaceIds(userId);
        boolean enabled = !loverSpaceIds.isEmpty();
        return new CycleCareAccessResponse(
                enabled,
                enabled ? null : "NO_LOVER_SPACE",
                loverSpaceIds
        );
    }

    @Transactional
    public CycleCareProfileResponse getProfile(Long userId) {
        accessService.requireAccess(userId);
        return toProfileResponse(getOrCreateProfile(userId));
    }

    @Transactional
    public CycleCareProfileResponse upsertProfile(UpsertCycleCareProfileRequest request, Long userId) {
        accessService.requireAccess(userId);
        CycleCareProfile profile = getOrCreateProfile(userId);
        Long loverSpaceId = accessService.resolveLoverSpaceId(userId, request.getDefaultLoverSpaceId());
        profile.setDefaultLoverSpaceId(loverSpaceId);
        profile.setCycleLength(request.getCycleLength() == null ? profile.getCycleLength() : request.getCycleLength());
        profile.setPeriodLength(request.getPeriodLength() == null ? profile.getPeriodLength() : request.getPeriodLength());
        profile.setLastPeriodStartDate(request.getLastPeriodStartDate());
        profile.setReminderEnabled(request.getReminderEnabled() == null ? profile.getReminderEnabled() : request.getReminderEnabled());
        profile.setDailyAdviceEnabled(request.getDailyAdviceEnabled() == null ? profile.getDailyAdviceEnabled() : request.getDailyAdviceEnabled());
        profile.setShareLevel(normalizeShareLevel(request.getShareLevel(), profile.getShareLevel()));
        profile.setTimezone(StringUtils.hasText(request.getTimezone()) ? request.getTimezone().trim() : DEFAULT_TIMEZONE);
        profile.setPrivacyNoteVisibleToPartner(request.getPrivacyNoteVisibleToPartner() == null
                ? profile.getPrivacyNoteVisibleToPartner()
                : request.getPrivacyNoteVisibleToPartner());
        profile.setUpdatedAt(LocalDateTime.now());
        profileMapper.updateById(profile);
        return toProfileResponse(profile);
    }

    @Transactional
    public CycleCareProfileResponse updateShareSettings(UpdateCycleShareSettingsRequest request, Long userId) {
        accessService.requireAccess(userId);
        CycleCareProfile profile = getOrCreateProfile(userId);
        profile.setShareLevel(normalizeShareLevel(request.getShareLevel(), profile.getShareLevel()));
        profile.setUpdatedAt(LocalDateTime.now());
        profileMapper.updateById(profile);
        return toProfileResponse(profile);
    }

    @Transactional
    public CycleTodayResponse getToday(Long loverSpaceId, Long userId) {
        accessService.requireAccess(userId);
        CycleCareProfile profile = getOrCreateProfile(userId);
        Long resolvedSpaceId = accessService.resolveLoverSpaceId(userId, loverSpaceId == null ? profile.getDefaultLoverSpaceId() : loverSpaceId);
        ensureProfileSpace(profile, resolvedSpaceId);

        LocalDate today = LocalDate.now();
        List<CyclePeriodRecord> records = listActiveRecords(userId, resolvedSpaceId);
        List<CycleDailyLog> recentLogs = listLogs(userId, resolvedSpaceId, today.minusDays(45), today);
        CycleDailyLog todayLog = findLog(userId, today);
        CyclePredictionResult prediction = predictionService.predict(profile, records, today);
        warningService.generateWarnings(profile, records, recentLogs, prediction, today);
        CycleCareAdvice advice = adviceService.buildAdvice(prediction.getPhase(), todayLog);
        return buildTodayResponse(prediction, advice, warningService.listActiveWarnings(userId));
    }

    public List<CyclePeriodRecordResponse> listPeriodRecords(Long loverSpaceId, Integer page, Integer size, Long userId) {
        Long resolvedSpaceId = resolveOptionalLoverSpace(userId, loverSpaceId);
        LambdaQueryWrapper<CyclePeriodRecord> wrapper = new LambdaQueryWrapper<CyclePeriodRecord>()
                .eq(CyclePeriodRecord::getUserId, userId)
                .eq(CyclePeriodRecord::getStatus, ACTIVE_STATUS)
                .orderByDesc(CyclePeriodRecord::getStartDate)
                .orderByDesc(CyclePeriodRecord::getCreatedAt);
        if (resolvedSpaceId != null) {
            wrapper.eq(CyclePeriodRecord::getLoverSpaceId, resolvedSpaceId);
        }
        List<CyclePeriodRecord> records = periodRecordMapper.selectPage(page(page, size), wrapper).getRecords();
        List<CyclePeriodRecordResponse> responses = new ArrayList<CyclePeriodRecordResponse>();
        for (CyclePeriodRecord record : records) {
            responses.add(toPeriodRecordResponse(record));
        }
        return responses;
    }

    @Transactional
    public CyclePeriodRecordResponse createPeriodRecord(CreateCyclePeriodRecordRequest request, Long userId) {
        CycleCareProfile profile = getOrCreateProfile(userId);
        Long loverSpaceId = accessService.resolveLoverSpaceId(userId, request.getLoverSpaceId() == null ? profile.getDefaultLoverSpaceId() : request.getLoverSpaceId());
        validatePeriodRange(request.getStartDate(), request.getEndDate());
        LocalDateTime now = LocalDateTime.now();

        CyclePeriodRecord record = new CyclePeriodRecord();
        record.setUserId(userId);
        record.setLoverSpaceId(loverSpaceId);
        record.setStartDate(request.getStartDate());
        record.setEndDate(request.getEndDate());
        record.setFlowSummary(trimToNull(request.getFlowSummary()));
        record.setPainSummary(trimToNull(request.getPainSummary()));
        record.setColorSummary(trimToNull(request.getColorSummary()));
        record.setCycleLengthSnapshot(profile.getCycleLength());
        record.setPeriodLengthSnapshot(profile.getPeriodLength());
        record.setNote(trimToNull(request.getNote()));
        record.setStatus(ACTIVE_STATUS);
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        periodRecordMapper.insert(record);

        if (profile.getLastPeriodStartDate() == null || request.getStartDate().isAfter(profile.getLastPeriodStartDate())) {
            profile.setLastPeriodStartDate(request.getStartDate());
            profile.setDefaultLoverSpaceId(loverSpaceId);
            profile.setUpdatedAt(now);
            profileMapper.updateById(profile);
        }

        refreshWarnings(profile, userId, loverSpaceId);
        return toPeriodRecordResponse(record);
    }

    @Transactional
    public CyclePeriodRecordResponse updatePeriodRecord(Long recordId, UpdateCyclePeriodRecordRequest request, Long userId) {
        CyclePeriodRecord record = requireOwnActiveRecord(recordId, userId);
        CycleCareProfile profile = getOrCreateProfile(userId);
        Long loverSpaceId = accessService.resolveLoverSpaceId(userId, request.getLoverSpaceId() == null ? record.getLoverSpaceId() : request.getLoverSpaceId());
        validatePeriodRange(request.getStartDate(), request.getEndDate());

        record.setLoverSpaceId(loverSpaceId);
        record.setStartDate(request.getStartDate());
        record.setEndDate(request.getEndDate());
        record.setFlowSummary(trimToNull(request.getFlowSummary()));
        record.setPainSummary(trimToNull(request.getPainSummary()));
        record.setColorSummary(trimToNull(request.getColorSummary()));
        record.setCycleLengthSnapshot(profile.getCycleLength());
        record.setPeriodLengthSnapshot(profile.getPeriodLength());
        record.setNote(trimToNull(request.getNote()));
        record.setUpdatedAt(LocalDateTime.now());
        periodRecordMapper.updateById(record);
        updateLastPeriodStartDate(profile, userId);
        refreshWarnings(profile, userId, loverSpaceId);
        return toPeriodRecordResponse(record);
    }

    @Transactional
    public void deletePeriodRecord(Long recordId, Long userId) {
        CyclePeriodRecord record = requireOwnActiveRecord(recordId, userId);
        CycleCareProfile profile = getOrCreateProfile(userId);
        record.setStatus(DELETED_STATUS);
        record.setDeletedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        periodRecordMapper.updateById(record);
        updateLastPeriodStartDate(profile, userId);
        refreshWarnings(profile, userId, record.getLoverSpaceId());
    }

    public List<CycleDailyLogResponse> listDailyLogs(Long loverSpaceId, LocalDate startDate, LocalDate endDate, Long userId) {
        Long resolvedSpaceId = resolveOptionalLoverSpace(userId, loverSpaceId);
        LocalDate end = endDate == null ? LocalDate.now() : endDate;
        LocalDate start = startDate == null ? end.minusDays(30) : startDate;
        if (start.isAfter(end)) {
            throw new BusinessException(400, "Start date cannot be after end date");
        }
        List<CycleDailyLog> logs = listLogs(userId, resolvedSpaceId, start, end);
        List<CycleDailyLogResponse> responses = new ArrayList<CycleDailyLogResponse>();
        for (CycleDailyLog log : logs) {
            responses.add(toDailyLogResponse(log));
        }
        return responses;
    }

    public CycleDailyLogResponse getDailyLog(LocalDate date, Long userId) {
        accessService.requireAccess(userId);
        if (date == null) {
            throw new BusinessException(400, "Date is required");
        }
        CycleDailyLog log = findLog(userId, date);
        return log == null ? null : toDailyLogResponse(log);
    }

    @Transactional
    public CycleDailyLogResponse upsertDailyLog(LocalDate date, UpsertCycleDailyLogRequest request, Long userId) {
        if (date == null) {
            throw new BusinessException(400, "Date is required");
        }
        CycleCareProfile profile = getOrCreateProfile(userId);
        Long loverSpaceId = accessService.resolveLoverSpaceId(userId, request.getLoverSpaceId() == null ? profile.getDefaultLoverSpaceId() : request.getLoverSpaceId());
        CycleDailyLog log = findLog(userId, date);
        LocalDateTime now = LocalDateTime.now();
        if (log == null) {
            log = new CycleDailyLog();
            log.setUserId(userId);
            log.setLogDate(date);
            log.setCreatedAt(now);
        }
        log.setLoverSpaceId(loverSpaceId);
        log.setFlowLevel(StringUtils.hasText(request.getFlowLevel()) ? request.getFlowLevel().trim() : FLOW_NONE);
        log.setBloodColor(trimToNull(request.getBloodColor()));
        log.setPainLevel(request.getPainLevel());
        log.setMood(trimToNull(request.getMood()));
        log.setSymptoms(writeSymptoms(request.getSymptoms()));
        log.setTemperatureFeeling(trimToNull(request.getTemperatureFeeling()));
        log.setAppetite(trimToNull(request.getAppetite()));
        log.setSleepHours(request.getSleepHours());
        log.setWaterCups(request.getWaterCups());
        log.setExerciseMinutes(request.getExerciseMinutes());
        log.setFoodTags(writeStringList(request.getFoodTags()));
        log.setMedicationNote(trimToNull(request.getMedicationNote()));
        log.setDischargeNote(trimToNull(request.getDischargeNote()));
        log.setTemperature(request.getTemperature());
        log.setWeight(request.getWeight());
        log.setNote(trimToNull(request.getNote()));
        log.setUpdatedAt(now);
        if (log.getId() == null) {
            dailyLogMapper.insert(log);
        } else {
            dailyLogMapper.updateById(log);
        }
        refreshWarnings(profile, userId, loverSpaceId);
        return toDailyLogResponse(log);
    }

    public List<CycleWarningResponse> listWarnings(Long userId) {
        accessService.requireAccess(userId);
        List<CycleWarningResponse> responses = new ArrayList<CycleWarningResponse>();
        for (CycleWarning warning : warningService.listActiveWarnings(userId)) {
            responses.add(toWarningResponse(warning));
        }
        return responses;
    }

    @Transactional
    public void dismissWarning(Long warningId, Long userId) {
        accessService.requireAccess(userId);
        CycleWarning warning = warningService.requireOwnActiveWarning(warningId, userId);
        if (warning == null) {
            throw new BusinessException(404, "Warning not found");
        }
        warningService.dismissWarning(warning);
    }

    public List<CycleCalendarEventResponse> getCalendarEvents(Long loverSpaceId, Integer year, Integer month, Long userId) {
        CycleCareProfile profile = getOrCreateProfile(userId);
        Long resolvedSpaceId = accessService.resolveLoverSpaceId(userId, loverSpaceId == null ? profile.getDefaultLoverSpaceId() : loverSpaceId);
        YearMonth yearMonth;
        try {
            yearMonth = YearMonth.of(year == null ? YearMonth.now().getYear() : year, month == null ? YearMonth.now().getMonthValue() : month);
        } catch (DateTimeException ex) {
            throw new BusinessException(400, "Invalid year or month");
        }
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        List<CyclePeriodRecord> records = listActiveRecords(userId, resolvedSpaceId);
        List<CycleCalendarEventResponse> events = new ArrayList<CycleCalendarEventResponse>();
        for (CyclePeriodRecord record : records) {
            LocalDate recordEnd = record.getEndDate() == null ? record.getStartDate() : record.getEndDate();
            if (record.getStartDate() == null || recordEnd.isBefore(start) || record.getStartDate().isAfter(end)) {
                continue;
            }
            events.add(new CycleCalendarEventResponse(
                    record.getId(),
                    "PERIOD",
                    "生理期记录",
                    record.getStartDate(),
                    false,
                    record.getLoverSpaceId(),
                    Map.of("endDate", recordEnd.toString())
            ));
        }
        CyclePredictionResult prediction = predictionService.predict(profile, records, LocalDate.now());
        LocalDate predictedStart = prediction.getPredictedNextStartDate();
        if (predictedStart != null && !predictedStart.isBefore(start) && !predictedStart.isAfter(end)) {
            events.add(new CycleCalendarEventResponse(
                    null,
                    "PREDICTED_PERIOD",
                    "预计生理期",
                    predictedStart,
                    true,
                    resolvedSpaceId,
                    Map.of("endDate", prediction.getPredictedNextEndDate() == null ? predictedStart.toString() : prediction.getPredictedNextEndDate().toString())
            ));
        }
        return events;
    }

    public CyclePartnerSummaryResponse getPartnerSummary(Long loverSpaceId, Long userId) {
        Long resolvedSpaceId = accessService.resolveLoverSpaceId(userId, loverSpaceId);
        List<CycleCareProfile> profiles = profileMapper.selectList(new LambdaQueryWrapper<CycleCareProfile>()
                .eq(CycleCareProfile::getDefaultLoverSpaceId, resolvedSpaceId)
                .ne(CycleCareProfile::getUserId, userId));
        for (CycleCareProfile profile : profiles) {
            String shareLevel = normalizeShareLevel(profile.getShareLevel(), PRIVATE_SHARE);
            if (!PRIVATE_SHARE.equals(shareLevel) && !CALENDAR_ONLY_SHARE.equals(shareLevel)) {
                List<CyclePeriodRecord> records = listActiveRecords(profile.getUserId(), resolvedSpaceId);
                CyclePredictionResult prediction = predictionService.predict(profile, records, LocalDate.now());
                CycleCareAdvice advice = adviceService.buildAdvice(prediction.getPhase(), null);
                boolean summaryOnly = BASIC_SHARE.equals(shareLevel) || SUMMARY_SHARE.equals(shareLevel);
                String title = summaryOnly ? phaseLabel(prediction.getPhase()) : advice.getTitle();
                String careAdvice = summaryOnly ? advice.getPartnerAdvice() : advice.getReminder();
                return new CyclePartnerSummaryResponse(true, shareLevel, title, careAdvice, CycleCareAdviceService.DISCLAIMER);
            }
        }
        return new CyclePartnerSummaryResponse(false, PRIVATE_SHARE, null, null, CycleCareAdviceService.DISCLAIMER);
    }

    public CycleParseLogResponse parseLogText(String text, Long userId) {
        accessService.requireAccess(userId);
        String value = StringUtils.hasText(text) ? text.trim() : "";
        String lower = value.toLowerCase(Locale.ROOT);
        List<String> symptoms = new ArrayList<String>();
        List<String> foodTags = new ArrayList<String>();

        String flowLevel = null;
        if (containsAny(value, "量中", "中等", "medium")) {
            flowLevel = "MEDIUM";
        } else if (containsAny(value, "量少", "少量", "light")) {
            flowLevel = "LIGHT";
        } else if (containsAny(value, "量很多", "很多", "非常多", "very heavy")) {
            flowLevel = "VERY_HEAVY";
        } else if (containsAny(value, "量多", "较多", "heavy")) {
            flowLevel = "HEAVY";
        }

        String bloodColor = null;
        if (containsAny(value, "鲜红", "亮红")) {
            bloodColor = "BRIGHT_RED";
        } else if (containsAny(value, "暗红", "深红")) {
            bloodColor = "DARK_RED";
        } else if (containsAny(value, "褐色", "棕色")) {
            bloodColor = "BROWN";
        } else if (containsAny(value, "粉色")) {
            bloodColor = "PINK";
        }

        Integer painLevel = parsePainLevel(value);
        if (painLevel == null && containsAny(value, "有点痛", "轻微痛", "微痛")) {
            painLevel = 3;
        } else if (painLevel == null && containsAny(value, "很痛", "疼得厉害", "剧痛")) {
            painLevel = 8;
        } else if (painLevel == null && containsAny(value, "痛", "疼", "肚子疼", "腹痛")) {
            painLevel = 4;
        }

        String mood = null;
        if (containsAny(value, "焦虑", "紧张")) {
            mood = "ANXIOUS";
        } else if (containsAny(value, "烦", "烦躁", "易怒")) {
            mood = "IRRITABLE";
        } else if (containsAny(value, "低落", "难过", "伤心")) {
            mood = "SAD";
        } else if (containsAny(value, "压力", "压抑")) {
            mood = "STRESSED";
        } else if (containsAny(value, "累", "疲惫", "困")) {
            mood = "TIRED";
        } else if (containsAny(value, "开心", "高兴")) {
            mood = "HAPPY";
        } else if (containsAny(value, "平静", "还好")) {
            mood = "CALM";
        }

        if (containsAny(value, "肚子痛", "肚子疼", "腹痛", "痛经", "cramp")) {
            symptoms.add("cramps");
        }
        if (containsAny(value, "头痛", "头疼")) {
            symptoms.add("headache");
        }
        if (containsAny(value, "腰酸", "腰疼", "腰痛")) {
            symptoms.add("backache");
        }
        if (containsAny(value, "腹胀", "胀气")) {
            symptoms.add("bloating");
        }
        if (containsAny(value, "发热", "发烧")) {
            symptoms.add("fever");
        }

        if (containsAny(value, "清淡")) {
            foodTags.add("light");
        }
        if (containsAny(value, "辛辣", "辣")) {
            foodTags.add("spicy");
        }
        if (containsAny(value, "冷饮", "冰")) {
            foodTags.add("cold");
        }

        Double sleepHours = parseNumberBeforeUnit(lower, "小时", "h");
        Integer waterCups = parseIntegerBeforeUnit(value, "杯");
        Integer exerciseMinutes = parseIntegerBeforeUnit(value, "分钟");

        return new CycleParseLogResponse(
                flowLevel,
                bloodColor,
                painLevel,
                mood,
                sleepHours,
                waterCups,
                exerciseMinutes,
                symptoms,
                foodTags,
                value,
                true,
                CycleCareAdviceService.DISCLAIMER
        );
    }

    private CycleCareProfile getOrCreateProfile(Long userId) {
        CycleCareProfile profile = profileMapper.selectOne(new LambdaQueryWrapper<CycleCareProfile>()
                .eq(CycleCareProfile::getUserId, userId)
                .last("LIMIT 1"));
        if (profile != null) {
            fillProfileDefaults(profile);
            return profile;
        }
        Long loverSpaceId = accessService.resolveLoverSpaceId(userId, null);
        LocalDateTime now = LocalDateTime.now();
        profile = new CycleCareProfile();
        profile.setUserId(userId);
        profile.setDefaultLoverSpaceId(loverSpaceId);
        profile.setCycleLength(28);
        profile.setPeriodLength(5);
        profile.setReminderEnabled(true);
        profile.setDailyAdviceEnabled(true);
        profile.setShareLevel(PRIVATE_SHARE);
        profile.setTimezone(DEFAULT_TIMEZONE);
        profile.setPrivacyNoteVisibleToPartner(false);
        profile.setCreatedAt(now);
        profile.setUpdatedAt(now);
        profileMapper.insert(profile);
        return profile;
    }

    private void fillProfileDefaults(CycleCareProfile profile) {
        boolean changed = false;
        if (profile.getCycleLength() == null) {
            profile.setCycleLength(28);
            changed = true;
        }
        if (profile.getPeriodLength() == null) {
            profile.setPeriodLength(5);
            changed = true;
        }
        if (profile.getReminderEnabled() == null) {
            profile.setReminderEnabled(true);
            changed = true;
        }
        if (profile.getDailyAdviceEnabled() == null) {
            profile.setDailyAdviceEnabled(true);
            changed = true;
        }
        if (!StringUtils.hasText(profile.getShareLevel())) {
            profile.setShareLevel(PRIVATE_SHARE);
            changed = true;
        }
        if (!StringUtils.hasText(profile.getTimezone())) {
            profile.setTimezone(DEFAULT_TIMEZONE);
            changed = true;
        }
        if (profile.getPrivacyNoteVisibleToPartner() == null) {
            profile.setPrivacyNoteVisibleToPartner(false);
            changed = true;
        }
        if (profile.getDefaultLoverSpaceId() == null) {
            profile.setDefaultLoverSpaceId(accessService.resolveLoverSpaceId(profile.getUserId(), null));
            changed = true;
        }
        if (changed) {
            profile.setUpdatedAt(LocalDateTime.now());
            profileMapper.updateById(profile);
        }
    }

    private void ensureProfileSpace(CycleCareProfile profile, Long loverSpaceId) {
        if (profile.getDefaultLoverSpaceId() == null || !profile.getDefaultLoverSpaceId().equals(loverSpaceId)) {
            profile.setDefaultLoverSpaceId(loverSpaceId);
            profile.setUpdatedAt(LocalDateTime.now());
            profileMapper.updateById(profile);
        }
    }

    private Long resolveOptionalLoverSpace(Long userId, Long loverSpaceId) {
        accessService.requireAccess(userId);
        if (loverSpaceId == null) {
            return null;
        }
        return accessService.resolveLoverSpaceId(userId, loverSpaceId);
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

    private CycleDailyLog findLog(Long userId, LocalDate date) {
        return dailyLogMapper.selectOne(new LambdaQueryWrapper<CycleDailyLog>()
                .eq(CycleDailyLog::getUserId, userId)
                .eq(CycleDailyLog::getLogDate, date)
                .last("LIMIT 1"));
    }

    private CyclePeriodRecord requireOwnActiveRecord(Long recordId, Long userId) {
        CyclePeriodRecord record = periodRecordMapper.selectById(recordId);
        if (record == null || !userId.equals(record.getUserId()) || !ACTIVE_STATUS.equals(record.getStatus())) {
            throw new BusinessException(404, "Period record not found");
        }
        return record;
    }

    private void updateLastPeriodStartDate(CycleCareProfile profile, Long userId) {
        CyclePeriodRecord latest = periodRecordMapper.selectOne(new LambdaQueryWrapper<CyclePeriodRecord>()
                .eq(CyclePeriodRecord::getUserId, userId)
                .eq(CyclePeriodRecord::getStatus, ACTIVE_STATUS)
                .orderByDesc(CyclePeriodRecord::getStartDate)
                .last("LIMIT 1"));
        profile.setLastPeriodStartDate(latest == null ? null : latest.getStartDate());
        profile.setUpdatedAt(LocalDateTime.now());
        profileMapper.updateById(profile);
    }

    private void refreshWarnings(CycleCareProfile profile, Long userId, Long loverSpaceId) {
        LocalDate today = LocalDate.now();
        List<CyclePeriodRecord> records = listActiveRecords(userId, loverSpaceId);
        CyclePredictionResult prediction = predictionService.predict(profile, records, today);
        List<CycleDailyLog> logs = listLogs(userId, loverSpaceId, today.minusDays(45), today);
        warningService.generateWarnings(profile, records, logs, prediction, today);
    }

    private void validatePeriodRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            throw new BusinessException(400, "Start date is required");
        }
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new BusinessException(400, "End date cannot be before start date");
        }
        if (endDate != null && ChronoUnit.DAYS.between(startDate, endDate) > 30) {
            throw new BusinessException(400, "Period record range is too long");
        }
    }

    private CycleTodayResponse buildTodayResponse(CyclePredictionResult prediction, CycleCareAdvice advice, List<CycleWarning> warnings) {
        List<CycleWarningResponse> warningResponses = new ArrayList<CycleWarningResponse>();
        for (CycleWarning warning : warnings) {
            warningResponses.add(toWarningResponse(warning));
        }
        return new CycleTodayResponse(
                prediction.getPhase(),
                phaseLabel(prediction.getPhase()),
                prediction.getDaysToNextPeriod(),
                prediction.getBasedOnHistory(),
                prediction.getPredictedPeriod(),
                prediction.getPredictedNextStartDate(),
                prediction.getPredictedNextEndDate(),
                advice.getTitle(),
                advice.getReminder(),
                advice.getClothingAdvice(),
                advice.getFoodAdvice(),
                advice.getRestAdvice(),
                advice.getMoodAdvice(),
                advice.getPartnerAdvice(),
                CycleCareAdviceService.DISCLAIMER,
                warningResponses
        );
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

    private String normalizeShareLevel(String shareLevel, String fallback) {
        if (!StringUtils.hasText(shareLevel)) {
            return StringUtils.hasText(fallback) ? fallback : PRIVATE_SHARE;
        }
        String value = shareLevel.trim().toUpperCase();
        if (BASIC_SHARE.equals(value)) {
            return SUMMARY_SHARE;
        }
        if (DETAILED_SHARE.equals(value)) {
            return FULL_SHARE;
        }
        if (!PRIVATE_SHARE.equals(value) && !SUMMARY_SHARE.equals(value) && !CALENDAR_ONLY_SHARE.equals(value) && !FULL_SHARE.equals(value)) {
            throw new BusinessException(400, "Invalid share level");
        }
        return value;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String writeSymptoms(List<String> symptoms) {
        return writeStringList(symptoms);
    }

    private String writeStringList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(values);
        } catch (Exception ex) {
            throw new BusinessException(400, "Invalid list value");
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

    private boolean containsAny(String value, String... keywords) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        String lower = value.toLowerCase(Locale.ROOT);
        for (String keyword : keywords) {
            if (StringUtils.hasText(keyword) && lower.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private Integer parsePainLevel(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("(疼痛|痛|疼|pain)\\D{0,6}(10|[0-9])")
                .matcher(value);
        if (matcher.find()) {
            return Math.min(10, Math.max(0, Integer.parseInt(matcher.group(2))));
        }
        return null;
    }

    private Double parseNumberBeforeUnit(String value, String... units) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        for (String unit : units) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern
                    .compile("(\\d+(?:\\.\\d+)?)\\s*(?:个)?\\s*" + java.util.regex.Pattern.quote(unit))
                    .matcher(value);
            if (matcher.find()) {
                return Double.parseDouble(matcher.group(1));
            }
        }
        return null;
    }

    private Integer parseIntegerBeforeUnit(String value, String unit) {
        Double number = parseNumberBeforeUnit(value, unit);
        return number == null ? null : number.intValue();
    }

    private <T> Page<T> page(Integer page, Integer size) {
        long current = page == null || page < 1 ? 1L : page.longValue();
        long pageSize = size == null || size < 1 ? 20L : Math.min(size.longValue(), 100L);
        return new Page<T>(current, pageSize);
    }

    private CycleCareProfileResponse toProfileResponse(CycleCareProfile profile) {
        return new CycleCareProfileResponse(
                profile.getId(),
                profile.getUserId(),
                profile.getDefaultLoverSpaceId(),
                profile.getCycleLength(),
                profile.getPeriodLength(),
                profile.getLastPeriodStartDate(),
                profile.getReminderEnabled(),
                profile.getDailyAdviceEnabled(),
                normalizeShareLevel(profile.getShareLevel(), PRIVATE_SHARE),
                profile.getTimezone(),
                profile.getPrivacyNoteVisibleToPartner(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    private CyclePeriodRecordResponse toPeriodRecordResponse(CyclePeriodRecord record) {
        return new CyclePeriodRecordResponse(
                record.getId(),
                record.getLoverSpaceId(),
                record.getStartDate(),
                record.getEndDate(),
                record.getFlowSummary(),
                record.getPainSummary(),
                record.getColorSummary(),
                record.getCycleLengthSnapshot(),
                record.getPeriodLengthSnapshot(),
                record.getNote(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }

    private CycleDailyLogResponse toDailyLogResponse(CycleDailyLog log) {
        return new CycleDailyLogResponse(
                log.getId(),
                log.getLoverSpaceId(),
                log.getLogDate(),
                log.getFlowLevel(),
                log.getBloodColor(),
                log.getPainLevel(),
                log.getMood(),
                readSymptoms(log.getSymptoms()),
                log.getTemperatureFeeling(),
                log.getAppetite(),
                log.getSleepHours(),
                log.getWaterCups(),
                log.getExerciseMinutes(),
                readSymptoms(log.getFoodTags()),
                log.getMedicationNote(),
                log.getDischargeNote(),
                log.getTemperature(),
                log.getWeight(),
                log.getNote(),
                log.getCreatedAt(),
                log.getUpdatedAt()
        );
    }

    private CycleWarningResponse toWarningResponse(CycleWarning warning) {
        return new CycleWarningResponse(
                warning.getId(),
                warning.getLoverSpaceId(),
                warning.getWarningType(),
                warning.getWarningDate(),
                warning.getSeverity(),
                warning.getTitle(),
                warning.getMessage(),
                warning.getStatus(),
                warning.getCreatedAt()
        );
    }
}
