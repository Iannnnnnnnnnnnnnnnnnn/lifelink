package com.lifelink.calendar.controller;

import com.lifelink.calendar.dto.CalendarDayResponse;
import com.lifelink.calendar.dto.CalendarMonthQueryRequest;
import com.lifelink.calendar.dto.CalendarMonthResponse;
import com.lifelink.calendar.dto.CreateCalendarEventRequest;
import com.lifelink.calendar.dto.UpdateCalendarEventRequest;
import com.lifelink.calendar.service.CalendarService;
import com.lifelink.common.Result;
import com.lifelink.security.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping("/month")
    public Result<CalendarMonthResponse> getMonthCalendar(@ModelAttribute CalendarMonthQueryRequest request,
                                                          @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(calendarService.getMonthCalendar(request, loginUser.getId()));
    }

    @GetMapping("/day")
    public Result<CalendarDayResponse> getDayCalendar(@RequestParam Long relationshipId,
                                                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                      @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(calendarService.getDayCalendar(relationshipId, date, loginUser.getId()));
    }

    @PostMapping("/events")
    public Result<CalendarDayResponse> createCalendarEvent(@Valid @RequestBody CreateCalendarEventRequest request,
                                                           @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(calendarService.createCalendarEvent(request, loginUser.getId()));
    }

    @PutMapping("/events/{eventId}")
    public Result<CalendarDayResponse> updateCalendarEvent(@PathVariable Long eventId,
                                                           @Valid @RequestBody UpdateCalendarEventRequest request,
                                                           @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(calendarService.updateCalendarEvent(eventId, request, loginUser.getId()));
    }

    @DeleteMapping("/events/{eventId}")
    public Result<Void> deleteCalendarEvent(@PathVariable Long eventId,
                                            @AuthenticationPrincipal LoginUser loginUser) {
        calendarService.deleteCalendarEvent(eventId, loginUser.getId());
        return Result.success();
    }
}
