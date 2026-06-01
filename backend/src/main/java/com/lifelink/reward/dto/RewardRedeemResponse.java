package com.lifelink.reward.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RewardRedeemResponse {

    private RewardRedemptionResponse redemption;

    private Integer balance;
}
