package com.lifelink.anniversary.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnniversaryResponse {

    private Long id;
    private Long relationshipId;
    private String relationshipName;
    private String title;
    private String description;
    private LocalDate anniversaryDate;
    private String repeatType;
    private Long backgroundFileId;
    private String backgroundUrl;
    private Long dayCount;
    private String displayType;
    private Integer passedYears;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
