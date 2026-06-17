package com.lifelink.reward.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RewardRedemptionResponse {

    private Long id;

    private Long rewardId;

    private Integer coinCostSnapshot;

    private String rewardTitleSnapshot;

    private String rewardDescriptionSnapshot;

    private String rewardCoverUrlSnapshot;

    private String status;

    private String note;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
