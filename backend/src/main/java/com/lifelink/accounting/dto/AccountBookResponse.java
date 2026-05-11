package com.lifelink.accounting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountBookResponse {

    private Long id;

    private Long relationshipId;

    private String relationshipName;

    private Long ownerId;

    private String name;

    private String type;

    private String status;

    private LocalDateTime createdAt;
}
