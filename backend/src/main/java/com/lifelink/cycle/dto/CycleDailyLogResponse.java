package com.lifelink.cycle.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CycleDailyLogResponse {

    private Long id;

    private Long loverSpaceId;

    private LocalDate logDate;

    private String flowLevel;

    private Integer painLevel;

    private String mood;

    private List<String> symptoms;

    private String temperatureFeeling;

    private String appetite;

    private String note;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
