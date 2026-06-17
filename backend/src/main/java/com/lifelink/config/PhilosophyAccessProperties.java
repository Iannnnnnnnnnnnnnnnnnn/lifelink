package com.lifelink.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "lifelink.feature.philosophy")
public class PhilosophyAccessProperties {

    private String allowedPhones = "";
}
