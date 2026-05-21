package com.lifelink.philosophy.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelink.common.BusinessException;
import com.lifelink.philosophy.config.PhilosophyAiProperties;
import com.lifelink.philosophy.dto.PhilosophyResponseItem;
import com.lifelink.philosophy.entity.Philosopher;
import com.lifelink.philosophy.service.PhilosophyAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PhilosophyAiServiceImpl implements PhilosophyAiService {

    private static final String ZH_CN = "zh-CN";
    private static final int MAX_PROMPT_QUESTION_LENGTH = 1000;

    private final PhilosophyAiProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public PhilosophyResponseItem generate(String question, Philosopher philosopher, String language) {
        if (isMockMode()) {
            return mockResponse(question, philosopher, language);
        }
        try {
            String content = callAi(question, philosopher, language);
            PhilosophyResponseItem item = parseContent(content, philosopher, language);
            item.setRawResponse(content);
            return item;
        } catch (Exception ex) {
            throw new BusinessException(502, "AI generation failed");
        }
    }

    private boolean isMockMode() {
        return !StringUtils.hasText(properties.getApiKey())
                || "mock".equalsIgnoreCase(trimToEmpty(properties.getProvider()));
    }

    private String callAi(String question, Philosopher philosopher, String language) throws Exception {
        String safeQuestion = question.length() > MAX_PROMPT_QUESTION_LENGTH
                ? question.substring(0, MAX_PROMPT_QUESTION_LENGTH)
                : question;
        List<Map<String, String>> messages = new ArrayList<Map<String, String>>();
        messages.add(message("system", buildSystemPrompt(language)));
        messages.add(message("user", buildUserPrompt(safeQuestion, philosopher, language)));

        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("model", properties.getModel());
        payload.put("messages", messages);
        payload.put("temperature", 0.7);
        payload.put("max_tokens", properties.getMaxOutputTokens());
        payload.put("response_format", Map.of("type", "json_object"));

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds()))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getBaseUrl()))
                .timeout(Duration.ofSeconds(timeoutSeconds()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + properties.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new BusinessException(502, "AI provider request failed");
        }
        JsonNode root = objectMapper.readTree(response.body());
        JsonNode content = root.path("choices").path(0).path("message").path("content");
        if (!content.isTextual()) {
            throw new BusinessException(502, "Invalid AI provider response");
        }
        return content.asText();
    }

    private String buildSystemPrompt(String language) {
        String outputLanguage = ZH_CN.equals(language) ? "中文" : "English";
        return "You are a philosophical style analysis engine.\n"
                + "Important limits:\n"
                + "1. You are not the philosopher.\n"
                + "2. Do not claim to be a real historical figure.\n"
                + "3. Do not fabricate sources.\n"
                + "4. Do not invent quotations.\n"
                + "5. Only simulate analysis based on public philosophical styles.\n"
                + "6. Make the answer readable for ordinary users.\n"
                + "7. Do not output hateful, discriminatory, or aggressive content.\n"
                + "8. Do not encourage self-harm, illegal acts, or dangerous behavior.\n"
                + "9. You must return JSON only.\n"
                + "Return fields: viewpoint, questionBack, objection, summary.\n"
                + "Length guide: viewpoint 100-250 words/chars, questionBack 30-120, objection 60-160, summary 20-60.\n"
                + "Answer language: " + outputLanguage + ".";
    }

    private String buildUserPrompt(String question, Philosopher philosopher, String language) {
        String name = ZH_CN.equals(language) ? philosopher.getNameZh() : philosopher.getNameEn();
        String era = ZH_CN.equals(language) ? philosopher.getEraZh() : philosopher.getEraEn();
        String description = ZH_CN.equals(language) ? philosopher.getDescriptionZh() : philosopher.getDescriptionEn();
        return "Thinker code: " + philosopher.getCode() + "\n"
                + "Thinker name: " + name + "\n"
                + "Era: " + era + "\n"
                + "Style reference: " + description + "\n"
                + "User question: " + question + "\n"
                + "Return JSON only.";
    }

    private PhilosophyResponseItem parseContent(String content, Philosopher philosopher, String language) throws Exception {
        String json = extractJson(content);
        JsonNode root = objectMapper.readTree(json);
        PhilosophyResponseItem item = new PhilosophyResponseItem();
        item.setPhilosopherCode(philosopher.getCode());
        item.setPhilosopherName(resolveName(philosopher, language));
        item.setViewpoint(text(root, "viewpoint"));
        item.setQuestionBack(text(root, "questionBack"));
        item.setObjection(text(root, "objection"));
        item.setSummary(text(root, "summary"));
        return item;
    }

    private String extractJson(String content) {
        String trimmed = content == null ? "" : content.trim();
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        return trimmed;
    }

    private PhilosophyResponseItem mockResponse(String question, Philosopher philosopher, String language) {
        boolean zh = ZH_CN.equals(language);
        String name = resolveName(philosopher, language);
        String shortQuestion = question.length() > 80 ? question.substring(0, 80) : question;
        if (zh) {
            return new PhilosophyResponseItem(
                    philosopher.getCode(),
                    name,
                    "如果以" + name + "的思想风格来分析，这个问题不应只被看作一个待解决的事实，而应被视为一次自我理解的入口。你真正面对的也许不是“" + shortQuestion + "”本身，而是它背后关于价值、选择和生活秩序的判断。",
                    "当你提出这个问题时，你最想确认的是事实、意义，还是自己应当如何行动？",
                    "可能的反驳是：这种思想风格会把具体处境抽象化，忽略现实压力、情绪经验和人与人之间复杂的差异。",
                    "问题的答案，取决于你如何理解自己正在追求的生活。",
                    null
            );
        }
        return new PhilosophyResponseItem(
                philosopher.getCode(),
                name,
                "Read through the style of " + name + ", this question is not merely a problem to solve, but an entry into self-understanding. What you face may not be only \"" + shortQuestion + "\", but the values, choices, and order of life behind it.",
                "When you ask this, are you seeking facts, meaning, or guidance for action?",
                "A possible objection is that this style may abstract away from concrete conditions, emotional pressure, and the complexity of individual lives.",
                "The answer depends on what kind of life you believe is worth pursuing.",
                null
        );
    }

    private String resolveName(Philosopher philosopher, String language) {
        return ZH_CN.equals(language) ? philosopher.getNameZh() : philosopher.getNameEn();
    }

    private String text(JsonNode root, String field) {
        JsonNode value = root.path(field);
        return value.isTextual() ? value.asText() : "";
    }

    private Map<String, String> message(String role, String content) {
        Map<String, String> message = new LinkedHashMap<String, String>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private int timeoutSeconds() {
        return properties.getTimeoutSeconds() == null || properties.getTimeoutSeconds() <= 0
                ? 30
                : properties.getTimeoutSeconds();
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
