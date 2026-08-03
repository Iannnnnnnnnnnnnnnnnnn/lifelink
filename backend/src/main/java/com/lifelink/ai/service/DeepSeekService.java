package com.lifelink.ai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeepSeekService {

    private final DeepSeekClient deepSeekClient;

    public String generateAnswer(String systemPrompt, String question) {
        return deepSeekClient.chat(systemPrompt, question);
    }
}
