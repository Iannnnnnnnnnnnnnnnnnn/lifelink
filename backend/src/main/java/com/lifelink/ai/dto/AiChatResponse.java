package com.lifelink.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponse {

    private String answer;

    private List<Reference> references;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Reference {
        private String sourceType;
        private Long sourceId;
        private String content;
    }
}
