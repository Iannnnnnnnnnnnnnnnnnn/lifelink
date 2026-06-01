package com.lifelink.coin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoinAccountResponse {

    private Integer balance;

    private Integer totalEarned;

    private Integer totalSpent;
}
