package com.lifelink.daily.controller;

import com.lifelink.common.Result;
import com.lifelink.daily.dto.CreateDailyPostRequest;
import com.lifelink.daily.dto.DailyPostDetailResponse;
import com.lifelink.daily.dto.DailyPostResponse;
import com.lifelink.daily.service.DailyPostService;
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
@RequestMapping("/api/daily-posts")
@RequiredArgsConstructor
public class DailyPostController {

    private final DailyPostService dailyPostService;

    @PostMapping
    public Result<DailyPostDetailResponse> createPost(
            @Valid @RequestBody CreateDailyPostRequest request,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(dailyPostService.createPost(request, loginUser.getId()));
    }

    @GetMapping
    public Result<List<DailyPostResponse>> listPosts(
            @RequestParam(required = false) Long relationshipId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(dailyPostService.listPosts(relationshipId, page, size, loginUser.getId()));
    }

    @GetMapping("/{id}")
    public Result<DailyPostDetailResponse> getPost(
            @PathVariable Long id,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(dailyPostService.getPost(id, loginUser.getId()));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        dailyPostService.deletePost(id, loginUser.getId());
        return Result.success();
    }
}
