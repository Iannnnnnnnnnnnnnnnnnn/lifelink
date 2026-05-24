package com.lifelink.calendar.dto;

import lombok.Data;

@Data
public class CalendarMonthQueryRequest {

    private Long relationshipId;

    private Integer year;

    private Integer month;

    private Boolean includeTodos = true;

    private Boolean includeAnniversaries = true;

    private Boolean includeDailyPosts = true;

    private Boolean includeTransactions = true;

    private Boolean includeHolidays = true;

    private Boolean includeCustomEvents = true;
}
