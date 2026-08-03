package com.lifelink.ai.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ToString
@Component
@ConfigurationProperties(prefix = "ai.deepseek")
public class DeepSeekProperties {

    @ToString.Exclude
    private String apiKey;

    private String baseUrl = "https://api.deepseek.com/v1";

    private String model = "deepseek-chat";

    private Integer timeoutSeconds = 60;

    private Integer maxTokens = 1200;

    private Double temperature = 0.2;
}
