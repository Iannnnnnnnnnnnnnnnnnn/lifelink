package com.lifelink.philosophy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "lifelink.ai")
public class PhilosophyAiProperties {

    private String provider = "mock";

    private String apiKey;

    private String model = "gpt-4o-mini";

    private Integer timeoutSeconds = 30;

    private Integer maxOutputTokens = 800;

    private String baseUrl = "https://api.openai.com/v1/chat/completions";
}
