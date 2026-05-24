package com.lifelink.philosophy.controller;

import com.lifelink.common.Result;
import com.lifelink.philosophy.dto.CreatePhilosophyChatSessionRequest;
import com.lifelink.philosophy.dto.PhilosophyChatSessionResponse;
import com.lifelink.philosophy.dto.SendPhilosophyChatMessageRequest;
import com.lifelink.philosophy.dto.SendPhilosophyChatMessageResponse;
import com.lifelink.philosophy.dto.UpdatePhilosophyChatTitleRequest;
import com.lifelink.philosophy.service.PhilosophyChatService;
import com.lifelink.security.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/philosophy/chat")
@RequiredArgsConstructor
public class PhilosophyChatController {

    private final PhilosophyChatService chatService;

    @PostMapping("/sessions")
    public Result<PhilosophyChatSessionResponse> createSession(
            @Valid @RequestBody CreatePhilosophyChatSessionRequest request,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(chatService.createSession(request, loginUser.getId()));
    }

    @PostMapping("/sessions/{sessionId}/messages")
    public Result<SendPhilosophyChatMessageResponse> sendMessage(
            @PathVariable Long sessionId,
            @Valid @RequestBody SendPhilosophyChatMessageRequest request,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(chatService.sendMessage(sessionId, request, loginUser.getId()));
    }

    @GetMapping("/sessions")
    public Result<List<PhilosophyChatSessionResponse>> listSessions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(chatService.listSessions(loginUser.getId(), page, size));
    }

    @GetMapping("/sessions/{sessionId}")
    public Result<PhilosophyChatSessionResponse> getSessionDetail(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(chatService.getSessionDetail(sessionId, loginUser.getId()));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public Result<Void> deleteSession(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        chatService.deleteSession(sessionId, loginUser.getId());
        return Result.success();
    }

    @PutMapping("/sessions/{sessionId}/title")
    public Result<PhilosophyChatSessionResponse> updateTitle(
            @PathVariable Long sessionId,
            @Valid @RequestBody UpdatePhilosophyChatTitleRequest request,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(chatService.updateTitle(sessionId, request, loginUser.getId()));
    }
}
