package com.lifelink.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRequest {

    private String systemPrompt;

    private String userPrompt;

    private Double temperature;

    private Integer maxTokens;

    private Boolean responseFormatJson;

    private List<AiChatMessage> messages;

    public AiChatRequest(String systemPrompt, String userPrompt, Double temperature, Integer maxTokens, Boolean responseFormatJson) {
        this.systemPrompt = systemPrompt;
        this.userPrompt = userPrompt;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.responseFormatJson = responseFormatJson;
    }
}
