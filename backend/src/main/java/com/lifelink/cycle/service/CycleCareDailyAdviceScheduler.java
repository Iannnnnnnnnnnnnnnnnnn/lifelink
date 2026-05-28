package com.lifelink.cycle.service;

import com.lifelink.cycle.config.CycleCareDailyAdviceProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class CycleCareDailyAdviceScheduler {

    private static final String LOCK_KEY_PREFIX = "lifelink:cycle-care:daily-advice-job:";

    private final CycleCareDailyAdviceProperties properties;
    private final RedisJobLockService redisJobLockService;
    private final CycleCareDailyAdviceService dailyAdviceService;

    @Scheduled(cron = "${lifelink.cycle-care.daily-advice.cron:0 5 0 * * ?}", zone = "${lifelink.cycle-care.daily-advice.zone:Asia/Shanghai}")
    public void runDailyAdviceJob() {
        if (!Boolean.TRUE.equals(properties.getEnabled())) {
            return;
        }
        ZoneId zoneId = resolveZone(properties.getZone());
        LocalDate jobDate = LocalDate.now(zoneId);
        String lockKey = LOCK_KEY_PREFIX + jobDate;
        int lockMinutes = properties.getLockMinutes() == null || properties.getLockMinutes() <= 0 ? 30 : properties.getLockMinutes();
        String token = redisJobLockService.tryLock(lockKey, Duration.ofMinutes(lockMinutes));
        if (!StringUtils.hasText(token)) {
            log.info("Cycle care daily advice job skipped: lock exists, jobDate={}", jobDate);
            return;
        }
        Instant startedAt = Instant.now();
        try {
            log.info("Cycle care daily advice job started, jobDate={}", jobDate);
            CycleCareDailyAdviceJobSummary summary = dailyAdviceService.runDailyJob();
            long elapsedMillis = Duration.between(startedAt, Instant.now()).toMillis();
            log.info("Cycle care daily advice job finished, jobDate={}, total={}, success={}, failed={}, skipped={}, elapsedMs={}",
                    jobDate,
                    summary.getTotal(),
                    summary.getSuccess(),
                    summary.getFailed(),
                    summary.getSkipped(),
                    elapsedMillis);
        } finally {
            redisJobLockService.unlock(lockKey, token);
        }
    }

    private ZoneId resolveZone(String zone) {
        try {
            return ZoneId.of(StringUtils.hasText(zone) ? zone.trim() : "Asia/Shanghai");
        } catch (Exception ex) {
            return ZoneId.of("Asia/Shanghai");
        }
    }
}
