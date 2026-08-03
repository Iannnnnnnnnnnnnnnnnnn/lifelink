package com.lifelink.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelink.ai.config.DeepSeekProperties;
import com.lifelink.common.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DeepSeekClient {

    private final DeepSeekProperties properties;
    private final ObjectMapper objectMapper;

    public String chat(String systemPrompt, String userPrompt) {
        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new BusinessException(503, "DeepSeek API is not configured");
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(properties.getApiKey());

            Map<String, Object> payload = new LinkedHashMap<String, Object>();
            payload.put("model", properties.getModel());
            payload.put("messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userPrompt)
            ));
            payload.put("temperature", properties.getTemperature() == null ? 0.2 : properties.getTemperature());
            payload.put("max_tokens", properties.getMaxTokens() == null ? 1200 : properties.getMaxTokens());
            payload.put("stream", false);

            ResponseEntity<String> response = restTemplate().postForEntity(
                    resolveChatUrl(),
                    new HttpEntity<String>(objectMapper.writeValueAsString(payload), headers),
                    String.class
            );
            if (!response.getStatusCode().is2xxSuccessful() || !StringUtils.hasText(response.getBody())) {
                throw new BusinessException(502, "DeepSeek request failed");
            }
            JsonNode content = objectMapper.readTree(response.getBody()).path("choices").path(0).path("message").path("content");
            if (!content.isTextual() || !StringUtils.hasText(content.asText())) {
                throw new BusinessException(502, "DeepSeek returned empty content");
            }
            return content.asText().trim();
        } catch (BusinessException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new BusinessException(502, "DeepSeek request failed");
        } catch (Exception exception) {
            throw new BusinessException(502, "DeepSeek response is invalid");
        }
    }

    private RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        int timeoutMillis = Math.toIntExact(Duration.ofSeconds(timeoutSeconds()).toMillis());
        factory.setConnectTimeout(timeoutMillis);
        factory.setReadTimeout(timeoutMillis);
        return new RestTemplate(factory);
    }

    private String resolveChatUrl() {
        String baseUrl = StringUtils.hasText(properties.getBaseUrl()) ? properties.getBaseUrl().trim() : "https://api.deepseek.com/v1";
        if (baseUrl.endsWith("/chat/completions")) {
            return baseUrl;
        }
        return (baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl) + "/chat/completions";
    }

    private int timeoutSeconds() {
        return properties.getTimeoutSeconds() == null || properties.getTimeoutSeconds() <= 0 ? 60 : properties.getTimeoutSeconds();
    }
}
