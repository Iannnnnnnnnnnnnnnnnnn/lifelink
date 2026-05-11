package com.lifelink.activity.controller;

import com.lifelink.activity.dto.SpaceActivityResponse;
import com.lifelink.activity.service.SpaceActivityService;
import com.lifelink.common.Result;
import com.lifelink.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SpaceActivityController {

    private final SpaceActivityService spaceActivityService;

    @GetMapping("/api/relationships/{relationshipId}/activities")
    public Result<List<SpaceActivityResponse>> listRelationshipActivities(
            @PathVariable Long relationshipId,
            @RequestParam(required = false) String activityType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(spaceActivityService.listActivities(relationshipId, activityType, page, size, loginUser.getId()));
    }

    @GetMapping("/api/activities")
    public Result<List<SpaceActivityResponse>> listMyActivities(
            @RequestParam(required = false) String activityType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(spaceActivityService.listMyActivities(activityType, page, size, loginUser.getId()));
    }
}
