package com.lifelink.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResult {

    private String content;

    private String provider;

    private String model;

    private Boolean mock;

    private String rawResponse;
}
