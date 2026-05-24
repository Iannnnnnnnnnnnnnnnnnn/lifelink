package com.lifelink.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarDayResponse {

    private LocalDate date;

    private Integer dayOfWeek;

    private Boolean isToday;

    private Boolean isWeekend;

    private Boolean isHoliday;

    private Boolean isWorkday;

    private String lunarText;

    private List<String> holidayNames = new ArrayList<String>();

    private List<String> solarTermNames = new ArrayList<String>();

    private List<CalendarDayItemResponse> items = new ArrayList<CalendarDayItemResponse>();

    private Integer todoCount = 0;

    private Integer doneTodoCount = 0;

    private Integer dailyPostCount = 0;

    private Integer anniversaryCount = 0;

    private BigDecimal incomeAmount = BigDecimal.ZERO;

    private BigDecimal expenseAmount = BigDecimal.ZERO;
}
