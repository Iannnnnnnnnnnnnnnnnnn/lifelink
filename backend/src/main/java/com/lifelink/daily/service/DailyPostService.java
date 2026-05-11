package com.lifelink.daily.service;

import com.lifelink.daily.dto.CreateDailyPostRequest;
import com.lifelink.daily.dto.DailyPostDetailResponse;
import com.lifelink.daily.dto.DailyPostResponse;

import java.util.List;

public interface DailyPostService {

    DailyPostDetailResponse createPost(CreateDailyPostRequest request, Long userId);

    List<DailyPostResponse> listPosts(Long relationshipId, Integer page, Integer size, Long userId);

    DailyPostDetailResponse getPost(Long postId, Long userId);

    void deletePost(Long postId, Long userId);
}
