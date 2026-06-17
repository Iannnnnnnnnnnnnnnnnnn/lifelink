package com.lifelink.cycle.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CycleCareProfileResponse {

    private Long id;

    private Long userId;

    private Long defaultLoverSpaceId;

    private Integer cycleLength;

    private Integer periodLength;

    private LocalDate lastPeriodStartDate;

    private Boolean reminderEnabled;

    private Boolean dailyAdviceEnabled;

    private String shareLevel;

    private String timezone;

    private Boolean privacyNoteVisibleToPartner;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
