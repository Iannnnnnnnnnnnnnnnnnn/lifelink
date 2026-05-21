package com.lifelink.ai.service.impl;

import com.lifelink.ai.config.AiProperties;
import com.lifelink.ai.dto.AiChatRequest;
import com.lifelink.ai.dto.AiChatResult;
import com.lifelink.ai.service.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MockAiChatServiceImpl implements AiChatService {

    private final AiProperties properties;

    @Override
    public AiChatResult chat(AiChatRequest request) {
        String content = isEnglish(request)
                ? "{"
                + "\"viewpoint\":\"This is a simulated perspective based on a philosophical style. The first task is not to rush toward a conclusion, but to clarify the concepts, desires, and judgments hidden behind the question.\","
                + "\"questionBack\":\"Have you clearly defined the central concept in your question?\","
                + "\"objection\":\"Another view may argue that this issue cannot be understood from one angle alone and must be returned to its concrete context.\","
                + "\"summary\":\"What matters first is clarifying the question itself.\""
                + "}"
                : "{"
                + "\"viewpoint\":\"这是一个基于思想风格的模拟观点。真正需要处理的不是立刻给出结论，而是先看清问题背后的概念、欲望和判断。\","
                + "\"questionBack\":\"你是否已经定义清楚了问题中的核心概念？\","
                + "\"objection\":\"另一种观点可能认为，这个问题不能只从单一角度理解，还需要放回具体处境和关系中。\","
                + "\"summary\":\"真正重要的是先澄清问题本身。\""
                + "}";
        return new AiChatResult(content, properties.getProvider(), properties.getModel(), true, content);
    }

    private boolean isEnglish(AiChatRequest request) {
        String systemPrompt = request == null ? "" : String.valueOf(request.getSystemPrompt());
        String userPrompt = request == null ? "" : String.valueOf(request.getUserPrompt());
        return systemPrompt.contains("en-US") || userPrompt.contains("en-US");
    }
}
