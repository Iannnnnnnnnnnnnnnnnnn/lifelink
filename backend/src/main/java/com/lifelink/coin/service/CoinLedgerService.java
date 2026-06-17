package com.lifelink.coin.service;

import com.lifelink.coin.dto.CoinLedgerResponse;

import java.util.List;

public interface CoinLedgerService {

    List<CoinLedgerResponse> listMyLedger(String startDate, String endDate, Integer page, Integer pageSize, Long userId);
}
