package com.lifelink.ai.service;

import com.lifelink.ai.dto.AiChatRequest;
import com.lifelink.ai.dto.AiChatResponse;
import com.lifelink.ai.entity.MemoryVector;
import com.lifelink.ai.mapper.MemoryVectorMapper;
import com.lifelink.common.BusinessException;
import com.lifelink.relationship.service.RelationshipPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RagService {

    private static final int TOP_K = 5;

    private final RelationshipPermissionService relationshipPermissionService;
    private final MemoryVectorMapper memoryVectorMapper;
    private final EmbeddingService embeddingService;
    private final DeepSeekService deepSeekService;

    public AiChatResponse chat(AiChatRequest request, Long userId) {
        if (request.getSpaceId() == null) {
            throw new BusinessException(400, "spaceId is required");
        }
        if (!StringUtils.hasText(request.getQuestion())) {
            throw new BusinessException(400, "question is required");
        }
        String question = request.getQuestion().trim();
        if (question.length() > 1000) {
            throw new BusinessException(400, "question must not exceed 1000 characters");
        }

        relationshipPermissionService.requireActiveRelationshipMember(request.getSpaceId(), userId);
        List<MemoryVector> memories = memoryVectorMapper.search(
                userId,
                request.getSpaceId(),
                MemoryBuildService.toVectorLiteral(embeddingService.embed(question)),
                TOP_K
        );
        if (memories.isEmpty()) {
            throw new BusinessException(400, "No memory knowledge base found. Please build it first.");
        }

        String answer = deepSeekService.generateAnswer(buildSystemPrompt(memories), question);
        List<AiChatResponse.Reference> references = memories.stream()
                .map(memory -> new AiChatResponse.Reference(memory.getSourceType(), memory.getSourceId(), memory.getContent()))
                .toList();
        return new AiChatResponse(answer, references);
    }

    private String buildSystemPrompt(List<MemoryVector> memories) {
        StringBuilder context = new StringBuilder();
        for (int index = 0; index < memories.size(); index++) {
            MemoryVector memory = memories.get(index);
            context.append(index + 1).append(". [").append(memory.getSourceType()).append(" #")
                    .append(memory.getSourceId()).append("] ").append(memory.getContent()).append('\n');
        }
        return "\u4f60\u662fLifeLink\u4e2a\u4eba\u8bb0\u5fc6\u52a9\u624b\u3002\u53ea\u80fd\u6839\u636e\u4ee5\u4e0b\u7528\u6237\u8bb0\u5fc6\u56de\u7b54\uff0c\u4e0d\u8981\u4f7f\u7528\u5916\u90e8\u77e5\u8bc6\u6216\u7f16\u9020\u7ec6\u8282\u3002"
                + "\u5982\u679c\u8d44\u6599\u4e0d\u8db3\uff0c\u8bf7\u660e\u786e\u8bf4\u201c\u73b0\u6709\u8bb0\u5fc6\u4e2d\u6ca1\u6709\u8db3\u591f\u8d44\u6599\u201d\u3002\u8bf7\u4f7f\u7528\u81ea\u7136\u3001\u7b80\u6d01\u7684\u4e2d\u6587\u56de\u7b54\u3002\n\n\u7528\u6237\u8bb0\u5fc6\uff1a\n"
                + context;
    }
}
