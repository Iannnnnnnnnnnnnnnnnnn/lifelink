package com.lifelink.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "lifelink.focus-coin")
public class FocusCoinProperties {

    private boolean enabled = true;

    private int unitMinutes = 5;

    private int perUnit = 2;

    private int maxMinutesPerSession = 120;

    private String dailyCap = "";
}
