package com.lifelink.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarMonthResponse {

    private Integer year;

    private Integer month;

    private Long relationshipId;

    private List<CalendarDayResponse> days = new ArrayList<CalendarDayResponse>();
}
