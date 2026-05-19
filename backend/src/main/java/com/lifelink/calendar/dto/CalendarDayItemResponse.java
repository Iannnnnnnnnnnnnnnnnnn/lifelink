package com.lifelink.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarDayItemResponse {

    private Long id;

    private String type;

    private String title;

    private String description;

    private LocalDate date;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Boolean allDay;

    private Long relationshipId;

    private String targetType;

    private Long targetId;

    private String status;

    private String priority;

    private BigDecimal amount;

    private BigDecimal incomeAmount;

    private BigDecimal expenseAmount;

    private String color;

    private String icon;

    private Map<String, Object> metadata;
}
