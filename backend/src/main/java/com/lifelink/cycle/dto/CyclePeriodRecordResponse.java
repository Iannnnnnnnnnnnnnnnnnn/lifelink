package com.lifelink.cycle.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CyclePeriodRecordResponse {

    private Long id;

    private Long loverSpaceId;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer cycleLengthSnapshot;

    private Integer periodLengthSnapshot;

    private String note;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
