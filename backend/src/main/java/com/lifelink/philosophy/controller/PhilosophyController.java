package com.lifelink.philosophy.controller;

import com.lifelink.common.Result;
import com.lifelink.philosophy.dto.CreatePhilosophySessionRequest;
import com.lifelink.philosophy.dto.PhilosopherResponse;
import com.lifelink.philosophy.dto.PhilosophySessionResponse;
import com.lifelink.philosophy.service.PhilosophyService;
import com.lifelink.security.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/philosophy")
@RequiredArgsConstructor
public class PhilosophyController {

    private final PhilosophyService philosophyService;

    @GetMapping("/philosophers")
    public Result<List<PhilosopherResponse>> listPhilosophers(@RequestParam(required = false) String language) {
        return Result.success(philosophyService.listPhilosophers(language));
    }

    @PostMapping("/sessions")
    public Result<PhilosophySessionResponse> createSession(
            @Valid @RequestBody CreatePhilosophySessionRequest request,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(philosophyService.createSession(request, loginUser.getId()));
    }

    @GetMapping("/sessions")
    public Result<List<PhilosophySessionResponse>> listSessions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(philosophyService.listMySessions(loginUser.getId(), page, size));
    }

    @GetMapping("/sessions/{sessionId}")
    public Result<PhilosophySessionResponse> getSessionDetail(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(philosophyService.getSessionDetail(sessionId, loginUser.getId()));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public Result<Void> deleteSession(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        philosophyService.deleteSession(sessionId, loginUser.getId());
        return Result.success();
    }
}
