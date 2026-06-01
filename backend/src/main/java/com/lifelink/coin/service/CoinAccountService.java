package com.lifelink.coin.service;

import com.lifelink.coin.dto.CoinAccountResponse;
import com.lifelink.coin.entity.UserCoinAccount;

public interface CoinAccountService {

    CoinAccountResponse getAccount(Long userId);

    UserCoinAccount getOrCreateAccount(Long userId);

    Integer earn(Long userId, Integer amount, String sourceType, Long sourceId, String title, String description);

    Integer spend(Long userId, Integer amount, String sourceType, Long sourceId, String title, String description);
}
