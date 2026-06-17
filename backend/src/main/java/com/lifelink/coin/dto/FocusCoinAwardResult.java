package com.lifelink.coin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FocusCoinAwardResult {

    private Integer coins;

    private boolean awardedNow;
}
