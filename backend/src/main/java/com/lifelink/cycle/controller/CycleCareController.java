package com.lifelink.cycle.controller;

import com.lifelink.common.Result;
import com.lifelink.cycle.dto.CreateCyclePeriodRecordRequest;
import com.lifelink.cycle.dto.CycleCalendarEventResponse;
import com.lifelink.cycle.dto.CycleCareAccessResponse;
import com.lifelink.cycle.dto.CycleCareProfileResponse;
import com.lifelink.cycle.dto.CycleDailyAdviceReportResponse;
import com.lifelink.cycle.dto.CycleDailyLogResponse;
import com.lifelink.cycle.dto.CyclePartnerSummaryResponse;
import com.lifelink.cycle.dto.CyclePeriodRecordResponse;
import com.lifelink.cycle.dto.CycleTodayResponse;
import com.lifelink.cycle.dto.CycleWarningResponse;
import com.lifelink.cycle.dto.UpdateCyclePeriodRecordRequest;
import com.lifelink.cycle.dto.UpdateCycleShareSettingsRequest;
import com.lifelink.cycle.dto.UpsertCycleCareProfileRequest;
import com.lifelink.cycle.dto.UpsertCycleDailyLogRequest;
import com.lifelink.cycle.service.CycleCareDailyAdviceService;
import com.lifelink.cycle.service.CycleCareService;
import com.lifelink.security.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/cycle-care")
@RequiredArgsConstructor
public class CycleCareController {

    private final CycleCareService cycleCareService;
    private final CycleCareDailyAdviceService dailyAdviceService;

    @GetMapping("/access")
    public Result<CycleCareAccessResponse> getAccess(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(cycleCareService.getAccess(loginUser.getId()));
    }

    @GetMapping("/profile")
    public Result<CycleCareProfileResponse> getProfile(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(cycleCareService.getProfile(loginUser.getId()));
    }

    @PutMapping("/profile")
    public Result<CycleCareProfileResponse> upsertProfile(@Valid @RequestBody UpsertCycleCareProfileRequest request,
                                                          @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(cycleCareService.upsertProfile(request, loginUser.getId()));
    }

    @PatchMapping("/share-settings")
    public Result<CycleCareProfileResponse> updateShareSettings(@Valid @RequestBody UpdateCycleShareSettingsRequest request,
                                                                @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(cycleCareService.updateShareSettings(request, loginUser.getId()));
    }

    @GetMapping("/today")
    public Result<CycleTodayResponse> getToday(@RequestParam(required = false) Long loverSpaceId,
                                               @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(cycleCareService.getToday(loverSpaceId, loginUser.getId()));
    }

    @GetMapping("/period-records")
    public Result<List<CyclePeriodRecordResponse>> listPeriodRecords(@RequestParam(required = false) Long loverSpaceId,
                                                                     @RequestParam(defaultValue = "1") Integer page,
                                                                     @RequestParam(defaultValue = "20") Integer size,
                                                                     @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(cycleCareService.listPeriodRecords(loverSpaceId, page, size, loginUser.getId()));
    }

    @PostMapping("/period-records")
    public Result<CyclePeriodRecordResponse> createPeriodRecord(@Valid @RequestBody CreateCyclePeriodRecordRequest request,
                                                                @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(cycleCareService.createPeriodRecord(request, loginUser.getId()));
    }

    @PutMapping("/period-records/{recordId}")
    public Result<CyclePeriodRecordResponse> updatePeriodRecord(@PathVariable Long recordId,
                                                                @Valid @RequestBody UpdateCyclePeriodRecordRequest request,
                                                                @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(cycleCareService.updatePeriodRecord(recordId, request, loginUser.getId()));
    }

    @DeleteMapping("/period-records/{recordId}")
    public Result<Void> deletePeriodRecord(@PathVariable Long recordId,
                                           @AuthenticationPrincipal LoginUser loginUser) {
        cycleCareService.deletePeriodRecord(recordId, loginUser.getId());
        return Result.success();
    }

    @GetMapping("/daily-logs")
    public Result<List<CycleDailyLogResponse>> listDailyLogs(@RequestParam(required = false) Long loverSpaceId,
                                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                             @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(cycleCareService.listDailyLogs(loverSpaceId, startDate, endDate, loginUser.getId()));
    }

    @GetMapping("/daily-logs/{date}")
    public Result<CycleDailyLogResponse> getDailyLog(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                     @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(cycleCareService.getDailyLog(date, loginUser.getId()));
    }

    @PutMapping("/daily-logs/{date}")
    public Result<CycleDailyLogResponse> upsertDailyLog(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                        @Valid @RequestBody UpsertCycleDailyLogRequest request,
                                                        @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(cycleCareService.upsertDailyLog(date, request, loginUser.getId()));
    }

    @GetMapping("/warnings")
    public Result<List<CycleWarningResponse>> listWarnings(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(cycleCareService.listWarnings(loginUser.getId()));
    }

    @GetMapping("/daily-reports/latest")
    public Result<CycleDailyAdviceReportResponse> getLatestDailyReport(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(dailyAdviceService.getLatestReport(loginUser.getId()));
    }

    @GetMapping("/daily-reports")
    public Result<List<CycleDailyAdviceReportResponse>> listDailyReports(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                                         @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(dailyAdviceService.listReports(startDate, endDate, loginUser.getId()));
    }

    @GetMapping("/daily-reports/{date}")
    public Result<CycleDailyAdviceReportResponse> getDailyReport(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                                 @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(dailyAdviceService.getReport(date, loginUser.getId()));
    }

    @PostMapping("/daily-reports/{date}/regenerate")
    public Result<CycleDailyAdviceReportResponse> regenerateDailyReport(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                                        @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(dailyAdviceService.regenerate(date, loginUser.getId()));
    }

    @GetMapping("/partner/daily-reports/latest")
    public Result<CycleDailyAdviceReportResponse> getLatestPartnerDailyReport(@RequestParam Long spaceId,
                                                                              @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(dailyAdviceService.getLatestPartnerReport(spaceId, loginUser.getId()));
    }

    @PatchMapping("/warnings/{warningId}/dismiss")
    public Result<Void> dismissWarning(@PathVariable Long warningId,
                                       @AuthenticationPrincipal LoginUser loginUser) {
        cycleCareService.dismissWarning(warningId, loginUser.getId());
        return Result.success();
    }

    @GetMapping("/calendar")
    public Result<List<CycleCalendarEventResponse>> getCalendarEvents(@RequestParam(required = false) Long loverSpaceId,
                                                                      @RequestParam(required = false) Integer year,
                                                                      @RequestParam(required = false) Integer month,
                                                                      @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(cycleCareService.getCalendarEvents(loverSpaceId, year, month, loginUser.getId()));
    }

    @GetMapping("/partner-summary")
    public Result<CyclePartnerSummaryResponse> getPartnerSummary(@RequestParam Long loverSpaceId,
                                                                 @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(cycleCareService.getPartnerSummary(loverSpaceId, loginUser.getId()));
    }
}
