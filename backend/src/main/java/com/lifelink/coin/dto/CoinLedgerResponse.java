package com.lifelink.coin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoinLedgerResponse {

    private Long id;

    private Integer changeAmount;

    private Integer balanceAfter;

    private String type;

    private String sourceType;

    private Long sourceId;

    private String title;

    private String description;

    private LocalDateTime createdAt;
}
