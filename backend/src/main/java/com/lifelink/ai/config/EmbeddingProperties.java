package com.lifelink.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ai.embedding")
public class EmbeddingProperties {

    private String url;

    private Integer timeoutSeconds = 60;
}
