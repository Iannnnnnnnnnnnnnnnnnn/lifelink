package com.lifelink.focus.controller;

import com.lifelink.common.Result;
import com.lifelink.focus.dto.CreateFocusRoomRequest;
import com.lifelink.focus.dto.FocusCalendarEventResponse;
import com.lifelink.focus.dto.FocusRoomResponse;
import com.lifelink.focus.dto.FocusSessionResponse;
import com.lifelink.focus.dto.FocusSettingsResponse;
import com.lifelink.focus.dto.FocusStatsResponse;
import com.lifelink.focus.dto.StartFocusSessionRequest;
import com.lifelink.focus.dto.UpdateFocusSettingsRequest;
import com.lifelink.focus.service.FocusService;
import com.lifelink.security.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/focus")
@RequiredArgsConstructor
public class FocusController {

    private final FocusService focusService;

    @GetMapping("/settings")
    public Result<FocusSettingsResponse> getSettings(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(focusService.getSettings(loginUser.getId()));
    }

    @PutMapping("/settings")
    public Result<FocusSettingsResponse> updateSettings(@Valid @RequestBody UpdateFocusSettingsRequest request,
                                                        @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(focusService.updateSettings(request, loginUser.getId()));
    }

    @PostMapping("/sessions/start")
    public Result<FocusSessionResponse> startSession(@Valid @RequestBody StartFocusSessionRequest request,
                                                     @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(focusService.startSession(request, loginUser.getId()));
    }

    @PostMapping("/sessions/{id}/pause")
    public Result<FocusSessionResponse> pauseSession(@PathVariable Long id, @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(focusService.pauseSession(id, loginUser.getId()));
    }

    @PostMapping("/sessions/{id}/resume")
    public Result<FocusSessionResponse> resumeSession(@PathVariable Long id, @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(focusService.resumeSession(id, loginUser.getId()));
    }

    @PostMapping("/sessions/{id}/complete")
    public Result<FocusSessionResponse> completeSession(@PathVariable Long id, @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(focusService.completeSession(id, loginUser.getId()));
    }

    @PostMapping("/sessions/{id}/abandon")
    public Result<FocusSessionResponse> abandonSession(@PathVariable Long id, @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(focusService.abandonSession(id, loginUser.getId()));
    }

    @GetMapping("/sessions/current")
    public Result<FocusSessionResponse> getCurrentSession(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(focusService.getCurrentSession(loginUser.getId()));
    }

    @GetMapping("/sessions")
    public Result<List<FocusSessionResponse>> listSessions(@RequestParam(required = false) String startDate,
                                                           @RequestParam(required = false) String endDate,
                                                           @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(focusService.listSessions(startDate, endDate, loginUser.getId()));
    }

    @GetMapping("/stats/today")
    public Result<FocusStatsResponse> todayStats(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(focusService.getStats("today", loginUser.getId()));
    }

    @GetMapping("/stats/week")
    public Result<FocusStatsResponse> weekStats(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(focusService.getStats("week", loginUser.getId()));
    }

    @GetMapping("/stats/month")
    public Result<FocusStatsResponse> monthStats(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(focusService.getStats("month", loginUser.getId()));
    }

    @PostMapping("/rooms")
    public Result<FocusRoomResponse> createRoom(@Valid @RequestBody CreateFocusRoomRequest request,
                                                @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(focusService.createRoom(request, loginUser.getId()));
    }

    @GetMapping("/rooms/{id}")
    public Result<FocusRoomResponse> getRoom(@PathVariable Long id, @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(focusService.getRoom(id, loginUser.getId()));
    }

    @PostMapping("/rooms/{id}/join")
    public Result<FocusRoomResponse> joinRoom(@PathVariable Long id, @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(focusService.joinRoom(id, loginUser.getId()));
    }

    @PostMapping("/rooms/{id}/decline")
    public Result<FocusRoomResponse> declineRoom(@PathVariable Long id, @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(focusService.declineRoom(id, loginUser.getId()));
    }

    @PostMapping("/rooms/{id}/start")
    public Result<FocusRoomResponse> startRoom(@PathVariable Long id, @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(focusService.startRoom(id, loginUser.getId()));
    }

    @PostMapping("/rooms/{id}/complete")
    public Result<FocusRoomResponse> completeRoom(@PathVariable Long id, @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(focusService.completeRoom(id, loginUser.getId()));
    }

    @PostMapping("/rooms/{id}/abandon")
    public Result<FocusRoomResponse> abandonRoom(@PathVariable Long id, @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(focusService.abandonRoom(id, loginUser.getId()));
    }

    @GetMapping("/rooms/current")
    public Result<FocusRoomResponse> getCurrentRoom(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(focusService.getCurrentRoom(loginUser.getId()));
    }

    @GetMapping("/rooms")
    public Result<List<FocusRoomResponse>> listRooms(@RequestParam(required = false) Long spaceId,
                                                     @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(focusService.listRooms(spaceId, loginUser.getId()));
    }

    @GetMapping("/calendar")
    public Result<List<FocusCalendarEventResponse>> calendar(@RequestParam(required = false) Integer year,
                                                             @RequestParam(required = false) Integer month,
                                                             @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(focusService.getCalendarEvents(year, month, loginUser.getId()));
    }
}
