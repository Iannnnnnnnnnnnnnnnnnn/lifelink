package com.lifelink.coin.service;

import com.lifelink.coin.dto.FocusCoinAwardResult;
import com.lifelink.focus.entity.FocusSession;

public interface FocusCoinService {

    int calculateCoins(Integer actualMinutes);

    FocusCoinAwardResult awardForFocusSession(FocusSession session);
}
