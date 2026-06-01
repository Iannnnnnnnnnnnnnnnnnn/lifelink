package com.lifelink.coin.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lifelink.coin.dto.FocusCoinAwardResult;
import com.lifelink.coin.service.CoinAccountService;
import com.lifelink.coin.service.FocusCoinService;
import com.lifelink.config.FocusCoinProperties;
import com.lifelink.focus.entity.FocusSession;
import com.lifelink.focus.mapper.FocusSessionMapper;
import com.lifelink.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FocusCoinServiceImpl implements FocusCoinService {

    private static final String COMPLETED = "COMPLETED";
    private static final String FOCUS_SESSION = "FOCUS_SESSION";

    private final FocusCoinProperties properties;
    private final FocusSessionMapper focusSessionMapper;
    private final CoinAccountService coinAccountService;
    private final NotificationService notificationService;

    @Override
    public int calculateCoins(Integer actualMinutes) {
        if (!properties.isEnabled() || actualMinutes == null || actualMinutes <= 0) {
            return 0;
        }
        int unitMinutes = Math.max(1, properties.getUnitMinutes());
        int perUnit = Math.max(0, properties.getPerUnit());
        int maxMinutes = Math.max(0, properties.getMaxMinutesPerSession());
        int eligibleMinutes = Math.min(actualMinutes, maxMinutes);
        return eligibleMinutes / unitMinutes * perUnit;
    }

    @Override
    @Transactional
    public FocusCoinAwardResult awardForFocusSession(FocusSession session) {
        if (session == null || !COMPLETED.equals(session.getStatus())) {
            return new FocusCoinAwardResult(0, false);
        }
        int coins = calculateCoins(session.getActualMinutes());
        LocalDateTime now = LocalDateTime.now();
        int updated = focusSessionMapper.update(null, new LambdaUpdateWrapper<FocusSession>()
                .eq(FocusSession::getId, session.getId())
                .isNull(FocusSession::getCoinsAwardedAt)
                .set(FocusSession::getCoinsAwarded, coins)
                .set(FocusSession::getCoinsAwardedAt, now)
                .set(FocusSession::getUpdatedAt, now));
        if (updated == 0) {
            FocusSession existing = focusSessionMapper.selectById(session.getId());
            return new FocusCoinAwardResult(existing == null || existing.getCoinsAwarded() == null ? 0 : existing.getCoinsAwarded(), false);
        }

        session.setCoinsAwarded(coins);
        session.setCoinsAwardedAt(now);
        if (coins > 0) {
            coinAccountService.earn(session.getUserId(), coins, FOCUS_SESSION, session.getId(),
                    "获得专注币", "完成 " + session.getActualMinutes() + " 分钟专注");
            createNotificationSafely(session, coins);
        }
        return new FocusCoinAwardResult(coins, true);
    }

    private void createNotificationSafely(FocusSession session, int coins) {
        try {
            notificationService.createNotification(session.getUserId(), null, "FOCUS_COINS_EARNED", "获得专注币",
                    "你完成了一次专注，获得了 " + coins + " 专注币。", FOCUS_SESSION, session.getId(), session.getSpaceId(),
                    Map.of("coins", coins, "actualMinutes", session.getActualMinutes()));
        } catch (Exception ex) {
            log.warn("Create focus coin notification failed: {}", session.getId(), ex);
        }
    }
}
