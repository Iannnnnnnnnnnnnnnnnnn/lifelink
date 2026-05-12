package com.lifelink.daily.controller;

import com.lifelink.common.Result;
import com.lifelink.daily.dto.CommentDailyPostRequest;
import com.lifelink.daily.dto.DailyPostCommentResponse;
import com.lifelink.daily.dto.DailyPostInteractionResponse;
import com.lifelink.daily.service.DailyPostInteractionService;
import com.lifelink.security.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DailyPostInteractionController {

    private final DailyPostInteractionService dailyPostInteractionService;

    @PostMapping("/api/daily-posts/{postId}/like")
    public Result<DailyPostInteractionResponse> likePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(dailyPostInteractionService.likePost(postId, loginUser.getId()));
    }

    @DeleteMapping("/api/daily-posts/{postId}/like")
    public Result<DailyPostInteractionResponse> unlikePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(dailyPostInteractionService.unlikePost(postId, loginUser.getId()));
    }

    @PostMapping("/api/daily-posts/{postId}/comments")
    public Result<DailyPostCommentResponse> commentPost(
            @PathVariable Long postId,
            @Valid @RequestBody CommentDailyPostRequest request,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(dailyPostInteractionService.commentPost(postId, request, loginUser.getId()));
    }

    @GetMapping("/api/daily-posts/{postId}/comments")
    public Result<List<DailyPostCommentResponse>> listComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(dailyPostInteractionService.listComments(postId, page, size, loginUser.getId()));
    }

    @DeleteMapping("/api/daily-posts/{postId}/comments/{commentId}")
    public Result<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        dailyPostInteractionService.deleteComment(postId, commentId, loginUser.getId());
        return Result.success();
    }

    @GetMapping("/api/daily-posts/{postId}/interactions")
    public Result<DailyPostInteractionResponse> getInteractionSummary(
            @PathVariable Long postId,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(dailyPostInteractionService.getInteractionSummary(postId, loginUser.getId()));
    }
}
