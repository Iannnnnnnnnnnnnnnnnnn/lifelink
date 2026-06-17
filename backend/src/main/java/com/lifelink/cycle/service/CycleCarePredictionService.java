package com.lifelink.cycle.service;

import com.lifelink.cycle.entity.CycleCareProfile;
import com.lifelink.cycle.entity.CyclePeriodRecord;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CycleCarePredictionService {

    public static final String MENSTRUATION = "MENSTRUATION";
    public static final String FOLLICULAR = "FOLLICULAR";
    public static final String OVULATION = "OVULATION";
    public static final String LUTEAL = "LUTEAL";
    public static final String UNKNOWN = "UNKNOWN";

    private static final int DEFAULT_CYCLE_LENGTH = 28;
    private static final int DEFAULT_PERIOD_LENGTH = 5;

    public CyclePredictionResult predict(CycleCareProfile profile, List<CyclePeriodRecord> records, LocalDate today) {
        LocalDate baseStartDate = resolveLastStartDate(profile, records);
        if (baseStartDate == null) {
            return new CyclePredictionResult(UNKNOWN, null, null, defaultCycleLength(profile), defaultPeriodLength(profile), null, false, false);
        }

        int averageCycleLength = averageCycleLength(profile, records);
        int averagePeriodLength = averagePeriodLength(profile, records);
        LocalDate predictedStart = baseStartDate.plusDays(averageCycleLength);
        LocalDate predictedEnd = predictedStart.plusDays(averagePeriodLength - 1L);
        CyclePeriodRecord actual = findActualPeriod(records, today);
        boolean predictedPeriod = actual == null && !today.isBefore(predictedStart) && !today.isAfter(predictedEnd);
        String phase = resolvePhase(actual, today, baseStartDate, predictedStart, predictedEnd, averagePeriodLength);
        int daysToNextPeriod = (int) ChronoUnit.DAYS.between(today, predictedStart);
        return new CyclePredictionResult(
                phase,
                predictedStart,
                predictedEnd,
                averageCycleLength,
                averagePeriodLength,
                daysToNextPeriod,
                predictedPeriod,
                records != null && records.size() >= 2
        );
    }

    public boolean isPeriodDate(CycleCareProfile profile, List<CyclePeriodRecord> records, LocalDate date) {
        return findActualPeriod(records, date) != null
                || MENSTRUATION.equals(predict(profile, records, date).getPhase());
    }

    private LocalDate resolveLastStartDate(CycleCareProfile profile, List<CyclePeriodRecord> records) {
        List<CyclePeriodRecord> sorted = sortedRecords(records);
        if (!sorted.isEmpty()) {
            return sorted.get(0).getStartDate();
        }
        return profile == null ? null : profile.getLastPeriodStartDate();
    }

    private CyclePeriodRecord findActualPeriod(List<CyclePeriodRecord> records, LocalDate date) {
        for (CyclePeriodRecord record : sortedRecords(records)) {
            LocalDate start = record.getStartDate();
            LocalDate end = record.getEndDate() == null ? date : record.getEndDate();
            if (start != null && !date.isBefore(start) && !date.isAfter(end)) {
                return record;
            }
        }
        return null;
    }

    private String resolvePhase(CyclePeriodRecord actual, LocalDate today, LocalDate baseStartDate, LocalDate predictedStart,
                                LocalDate predictedEnd, int periodLength) {
        if (actual != null || (!today.isBefore(predictedStart) && !today.isAfter(predictedEnd))) {
            return MENSTRUATION;
        }
        LocalDate lastPeriodEnd = baseStartDate.plusDays(periodLength - 1L);
        LocalDate ovulation = predictedStart.minusDays(14);
        if (!today.isBefore(ovulation.minusDays(2)) && !today.isAfter(ovulation.plusDays(2))) {
            return OVULATION;
        }
        if (today.isAfter(lastPeriodEnd) && today.isBefore(ovulation.minusDays(2))) {
            return FOLLICULAR;
        }
        if (today.isAfter(ovulation.plusDays(2)) && today.isBefore(predictedStart)) {
            return LUTEAL;
        }
        if (today.isBefore(baseStartDate)) {
            return UNKNOWN;
        }
        return today.isBefore(predictedStart) ? FOLLICULAR : LUTEAL;
    }

    private int averageCycleLength(CycleCareProfile profile, List<CyclePeriodRecord> records) {
        List<CyclePeriodRecord> sorted = sortedRecords(records);
        List<Integer> lengths = new ArrayList<Integer>();
        for (int i = 0; i + 1 < sorted.size() && lengths.size() < 6; i++) {
            long days = ChronoUnit.DAYS.between(sorted.get(i + 1).getStartDate(), sorted.get(i).getStartDate());
            if (days > 0) {
                lengths.add((int) days);
            }
        }
        if (!lengths.isEmpty()) {
            int sum = 0;
            for (Integer length : lengths) {
                sum += length;
            }
            return Math.round((float) sum / lengths.size());
        }
        return defaultCycleLength(profile);
    }

    private int averagePeriodLength(CycleCareProfile profile, List<CyclePeriodRecord> records) {
        List<Integer> lengths = new ArrayList<Integer>();
        for (CyclePeriodRecord record : sortedRecords(records)) {
            if (record.getStartDate() != null && record.getEndDate() != null) {
                lengths.add((int) ChronoUnit.DAYS.between(record.getStartDate(), record.getEndDate()) + 1);
            }
            if (lengths.size() >= 6) {
                break;
            }
        }
        if (!lengths.isEmpty()) {
            int sum = 0;
            for (Integer length : lengths) {
                sum += length;
            }
            return Math.max(1, Math.round((float) sum / lengths.size()));
        }
        return defaultPeriodLength(profile);
    }

    private int defaultCycleLength(CycleCareProfile profile) {
        return profile == null || profile.getCycleLength() == null ? DEFAULT_CYCLE_LENGTH : profile.getCycleLength();
    }

    private int defaultPeriodLength(CycleCareProfile profile) {
        return profile == null || profile.getPeriodLength() == null ? DEFAULT_PERIOD_LENGTH : profile.getPeriodLength();
    }

    private List<CyclePeriodRecord> sortedRecords(List<CyclePeriodRecord> records) {
        List<CyclePeriodRecord> sorted = records == null ? new ArrayList<CyclePeriodRecord>() : new ArrayList<CyclePeriodRecord>(records);
        sorted.sort(Comparator.comparing(CyclePeriodRecord::getStartDate, Comparator.nullsLast(Comparator.reverseOrder())));
        return sorted;
    }
}
