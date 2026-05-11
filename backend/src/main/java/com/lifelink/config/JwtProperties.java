package com.lifelink.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "lifelink.jwt")
public class JwtProperties {

    private String secret;

    private Long expiration;
}
