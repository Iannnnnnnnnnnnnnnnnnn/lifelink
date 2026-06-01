package com.lifelink.reward.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RewardResponse {

    private Long id;

    private String title;

    private String description;

    private String coverObjectKey;

    private String coverUrl;

    private Integer coinCost;

    private Integer stock;

    private Integer redeemedCount;

    private String status;

    private Integer sortOrder;

    private Boolean available;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
