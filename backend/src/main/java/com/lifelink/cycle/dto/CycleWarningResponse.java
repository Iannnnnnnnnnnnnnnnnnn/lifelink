package com.lifelink.cycle.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CycleWarningResponse {

    private Long id;

    private Long loverSpaceId;

    private String warningType;

    private LocalDate warningDate;

    private String severity;

    private String title;

    private String message;

    private String status;

    private LocalDateTime createdAt;
}
