package com.lifelink.relationship.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelationshipDetailResponse {

    private Long id;

    private String name;

    private String type;

    private String description;

    private Long ownerId;

    private String status;

    private String currentUserRole;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
