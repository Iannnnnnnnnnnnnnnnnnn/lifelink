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
@ConfigurationProperties(prefix = "lifelink.ai")
public class AiProperties {

    private Boolean enabled = false;

    private String provider = "mock";

    private String baseUrl = "https://api.deepseek.com/v1";

    @ToString.Exclude
    private String apiKey;

    private String model = "deepseek-v4-flash";

    private Integer timeoutSeconds = 60;

    private Integer maxTokens = 1200;

    private Double temperature = 0.7;

    private Boolean mock = true;
}
