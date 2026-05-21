package com.lifelink.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRequest {

    private String systemPrompt;

    private String userPrompt;

    private Double temperature;

    private Integer maxTokens;

    private Boolean responseFormatJson;
}
