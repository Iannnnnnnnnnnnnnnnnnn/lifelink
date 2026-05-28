package com.lifelink.cycle.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "lifelink.cycle-care.daily-advice")
public class CycleCareDailyAdviceProperties {

    private Boolean enabled = true;

    private String cron = "0 5 0 * * ?";

    private String zone = "Asia/Shanghai";

    private Boolean useAi = true;

    private Integer maxRegenerateDays = 30;

    private Integer lockMinutes = 30;
}
