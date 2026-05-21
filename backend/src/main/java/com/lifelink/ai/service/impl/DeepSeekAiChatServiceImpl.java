package com.lifelink.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelink.ai.config.AiProperties;
import com.lifelink.ai.dto.AiChatRequest;
import com.lifelink.ai.dto.AiChatResult;
import com.lifelink.ai.service.AiChatService;
import com.lifelink.common.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Primary
@Service
@RequiredArgsConstructor
public class DeepSeekAiChatServiceImpl implements AiChatService {

    private static final String CHAT_COMPLETIONS_PATH = "/chat/completions";

    private final AiProperties properties;
    private final MockAiChatServiceImpl mockAiChatService;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void logAiStatus() {
        log.info("AI chat configured: provider={}, model={}, mock={}",
                properties.getProvider(),
                properties.getModel(),
                isMockMode());
    }

    @Override
    public AiChatResult chat(AiChatRequest request) {
        if (isMockMode()) {
            return mockAiChatService.chat(request);
        }
        try {
            String rawResponse = postChatCompletions(request);
            String content = extractContent(rawResponse);
            return new AiChatResult(content, properties.getProvider(), properties.getModel(), false, rawResponse);
        } catch (BusinessException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new BusinessException(502, "AI provider request failed");
        } catch (Exception exception) {
            throw new BusinessException(502, "AI provider response is invalid");
        }
    }

    private boolean isMockMode() {
        return !Boolean.TRUE.equals(properties.getEnabled())
                || Boolean.TRUE.equals(properties.getMock())
                || "mock".equalsIgnoreCase(properties.getProvider())
                || !StringUtils.hasText(properties.getApiKey());
    }

    private String postChatCompletions(AiChatRequest request) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(properties.getApiKey());

        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("model", properties.getModel());
        payload.put("messages", buildMessages(request));
        payload.put("temperature", resolveTemperature(request));
        payload.put("max_tokens", resolveMaxTokens(request));
        payload.put("stream", false);
        if (Boolean.TRUE.equals(request.getResponseFormatJson())) {
            payload.put("response_format", Map.of("type", "json_object"));
        }

        HttpEntity<String> entity = new HttpEntity<String>(objectMapper.writeValueAsString(payload), headers);
        ResponseEntity<String> response = restTemplate().postForEntity(resolveChatUrl(), entity, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new BusinessException(502, "AI provider request failed");
        }
        if (!StringUtils.hasText(response.getBody())) {
            throw new BusinessException(502, "AI provider returned empty response");
        }
        return response.getBody();
    }

    private String extractContent(String rawResponse) throws Exception {
        JsonNode root = objectMapper.readTree(rawResponse);
        JsonNode content = root.path("choices").path(0).path("message").path("content");
        if (!content.isTextual() || !StringUtils.hasText(content.asText())) {
            throw new BusinessException(502, "AI provider returned empty content");
        }
        return content.asText();
    }

    private List<Map<String, String>> buildMessages(AiChatRequest request) {
        List<Map<String, String>> messages = new ArrayList<Map<String, String>>();
        messages.add(message("system", request.getSystemPrompt()));
        messages.add(message("user", request.getUserPrompt()));
        return messages;
    }

    private Map<String, String> message(String role, String content) {
        Map<String, String> message = new LinkedHashMap<String, String>();
        message.put("role", role);
        message.put("content", content == null ? "" : content);
        return message;
    }

    private RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        int timeoutMillis = Math.toIntExact(Duration.ofSeconds(timeoutSeconds()).toMillis());
        factory.setConnectTimeout(timeoutMillis);
        factory.setReadTimeout(timeoutMillis);
        return new RestTemplate(factory);
    }

    private String resolveChatUrl() {
        String baseUrl = StringUtils.hasText(properties.getBaseUrl())
                ? properties.getBaseUrl().trim()
                : "https://api.deepseek.com/v1";
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1) + CHAT_COMPLETIONS_PATH;
        }
        return baseUrl + CHAT_COMPLETIONS_PATH;
    }

    private double resolveTemperature(AiChatRequest request) {
        if (request.getTemperature() != null) {
            return request.getTemperature();
        }
        return properties.getTemperature() == null ? 0.7 : properties.getTemperature();
    }

    private int resolveMaxTokens(AiChatRequest request) {
        if (request.getMaxTokens() != null && request.getMaxTokens() > 0) {
            return request.getMaxTokens();
        }
        return properties.getMaxTokens() == null || properties.getMaxTokens() <= 0 ? 1200 : properties.getMaxTokens();
    }

    private int timeoutSeconds() {
        return properties.getTimeoutSeconds() == null || properties.getTimeoutSeconds() <= 0
                ? 60
                : properties.getTimeoutSeconds();
    }
}
