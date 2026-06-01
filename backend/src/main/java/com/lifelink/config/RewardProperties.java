package com.lifelink.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "lifelink.reward")
public class RewardProperties {

    private String adminPhones = "";
}
