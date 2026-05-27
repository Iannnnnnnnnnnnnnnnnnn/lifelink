package com.lifelink.cycle.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CycleCalendarEventResponse {

    private Long id;

    private String type;

    private String title;

    private LocalDate date;

    private Boolean predicted;

    private Long loverSpaceId;

    private Map<String, Object> metadata;
}
