package com.lifelink.calendar.service;

import com.lifelink.calendar.dto.CalendarDayResponse;
import com.lifelink.calendar.dto.CalendarMonthQueryRequest;
import com.lifelink.calendar.dto.CalendarMonthResponse;
import com.lifelink.calendar.dto.CreateCalendarEventRequest;
import com.lifelink.calendar.dto.UpdateCalendarEventRequest;

import java.time.LocalDate;

public interface CalendarService {

    CalendarMonthResponse getMonthCalendar(CalendarMonthQueryRequest request, Long userId);

    CalendarDayResponse getDayCalendar(Long relationshipId, LocalDate date, Long userId);

    CalendarDayResponse createCalendarEvent(CreateCalendarEventRequest request, Long userId);

    CalendarDayResponse updateCalendarEvent(Long eventId, UpdateCalendarEventRequest request, Long userId);

    void deleteCalendarEvent(Long eventId, Long userId);
}
