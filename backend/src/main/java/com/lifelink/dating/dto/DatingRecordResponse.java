package com.lifelink.dating.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatingRecordResponse {

    private Long id;
    private Long relationshipId;
    private String relationshipName;
    private Integer sequenceNumber;
    private LocalDate datingDate;
    private List<String> activities;
    private String location;
    private String note;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
