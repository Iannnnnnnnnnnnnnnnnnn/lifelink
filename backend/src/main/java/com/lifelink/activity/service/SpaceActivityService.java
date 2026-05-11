package com.lifelink.activity.service;

import com.lifelink.activity.dto.SpaceActivityResponse;

import java.util.List;
import java.util.Map;

public interface SpaceActivityService {

    void createActivity(Long relationshipId, Long actorUserId, String activityType, String targetType, Long targetId, String title, String content, Map<String, Object> metadata);

    List<SpaceActivityResponse> listActivities(Long relationshipId, String activityType, Integer page, Integer size, Long userId);

    List<SpaceActivityResponse> listMyActivities(String activityType, Integer page, Integer size, Long userId);
}
