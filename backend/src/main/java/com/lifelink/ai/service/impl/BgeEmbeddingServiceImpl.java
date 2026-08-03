package com.lifelink.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelink.ai.config.EmbeddingProperties;
import com.lifelink.ai.service.EmbeddingService;
import com.lifelink.common.BusinessException;
import lombok.RequiredArgsConstructor;
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
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BgeEmbeddingServiceImpl implements EmbeddingService {

    private static final int EMBEDDING_DIMENSION = 1024;

    private final EmbeddingProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public float[] embed(String text) {
        if (!StringUtils.hasText(properties.getUrl())) {
            throw new BusinessException(503, "Embedding service is not configured");
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> payload = isTeiEndpoint()
                    ? Map.of("inputs", text)
                    : Map.of("input", text, "model", "bge-m3");
            ResponseEntity<String> response = restTemplate().postForEntity(
                    properties.getUrl().trim(),
                    new HttpEntity<String>(objectMapper.writeValueAsString(payload), headers),
                    String.class
            );
            if (!response.getStatusCode().is2xxSuccessful() || !StringUtils.hasText(response.getBody())) {
                throw new BusinessException(502, "Embedding service request failed");
            }
            return toEmbedding(objectMapper.readTree(response.getBody()));
        } catch (BusinessException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new BusinessException(502, "Embedding service request failed");
        } catch (Exception exception) {
            throw new BusinessException(502, "Embedding service response is invalid");
        }
    }

    private float[] toEmbedding(JsonNode root) {
        JsonNode vector = root.isArray() ? root : root.path("data").path(0).path("embedding");
        if (vector.isArray() && vector.size() > 0 && vector.get(0).isArray()) {
            vector = vector.get(0);
        }
        if (!vector.isArray()) {
            vector = root.path("embedding");
        }
        if (!vector.isArray()) {
            vector = root.path("embeddings").path(0);
        }
        if (!vector.isArray() || vector.size() != EMBEDDING_DIMENSION) {
            throw new BusinessException(502, "Embedding service must return a 1024-dimensional BGE-M3 vector");
        }
        float[] values = new float[EMBEDDING_DIMENSION];
        for (int i = 0; i < vector.size(); i++) {
            if (!vector.get(i).isNumber()) {
                throw new BusinessException(502, "Embedding service returned an invalid vector");
            }
            values[i] = vector.get(i).floatValue();
            if (!Float.isFinite(values[i])) {
                throw new BusinessException(502, "Embedding service returned an invalid vector");
            }
        }
        return values;
    }

    private boolean isTeiEndpoint() {
        String url = properties.getUrl().trim();
        return url.endsWith("/embed") || url.endsWith("/embed/");
    }

    private RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        int timeout = properties.getTimeoutSeconds() == null || properties.getTimeoutSeconds() <= 0 ? 60 : properties.getTimeoutSeconds();
        int timeoutMillis = Math.toIntExact(Duration.ofSeconds(timeout).toMillis());
        factory.setConnectTimeout(timeoutMillis);
        factory.setReadTimeout(timeoutMillis);
        return new RestTemplate(factory);
    }
}
