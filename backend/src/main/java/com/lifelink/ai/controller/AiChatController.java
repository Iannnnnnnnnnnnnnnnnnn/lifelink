package com.lifelink.ai.controller;

import com.lifelink.ai.dto.AiChatRequest;
import com.lifelink.ai.dto.AiChatResponse;
import com.lifelink.ai.dto.MemoryBuildResponse;
import com.lifelink.ai.service.MemoryBuildService;
import com.lifelink.ai.service.RagService;
import com.lifelink.common.Result;
import com.lifelink.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final MemoryBuildService memoryBuildService;
    private final RagService ragService;

    @PostMapping("/memory/build/{spaceId}")
    public Result<MemoryBuildResponse> buildMemory(@PathVariable Long spaceId,
                                                    @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(memoryBuildService.build(spaceId, loginUser.getId()));
    }

    @PostMapping("/chat")
    public Result<AiChatResponse> chat(@RequestBody AiChatRequest request,
                                       @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(ragService.chat(request, loginUser.getId()));
    }
}
