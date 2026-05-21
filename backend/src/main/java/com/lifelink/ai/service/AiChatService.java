package com.lifelink.ai.service;

import com.lifelink.ai.dto.AiChatRequest;
import com.lifelink.ai.dto.AiChatResult;

public interface AiChatService {

    AiChatResult chat(AiChatRequest request);
}
