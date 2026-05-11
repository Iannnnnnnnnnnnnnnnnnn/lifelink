package com.lifelink.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI lifelinkOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LifeLink API")
                        .version("0.0.1")
                        .description("LifeLink backend API documentation"));
    }
}
