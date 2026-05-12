package com.lifelink.daily.service;

import com.lifelink.daily.dto.CommentDailyPostRequest;
import com.lifelink.daily.dto.DailyPostCommentResponse;
import com.lifelink.daily.dto.DailyPostInteractionResponse;

import java.util.List;

public interface DailyPostInteractionService {

    DailyPostInteractionResponse likePost(Long postId, Long userId);

    DailyPostInteractionResponse unlikePost(Long postId, Long userId);

    DailyPostCommentResponse commentPost(Long postId, CommentDailyPostRequest request, Long userId);

    List<DailyPostCommentResponse> listComments(Long postId, Integer page, Integer size, Long userId);

    void deleteComment(Long postId, Long commentId, Long userId);

    DailyPostInteractionResponse getInteractionSummary(Long postId, Long userId);
}
