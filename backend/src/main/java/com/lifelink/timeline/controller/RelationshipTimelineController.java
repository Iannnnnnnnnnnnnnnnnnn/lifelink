package com.lifelink.timeline.controller;

import com.lifelink.common.Result;
import com.lifelink.security.LoginUser;
import com.lifelink.timeline.dto.CreateCustomTimelineEventRequest;
import com.lifelink.timeline.dto.RelationshipTimelineEventResponse;
import com.lifelink.timeline.service.RelationshipTimelineService;
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
public class RelationshipTimelineController {

    private final RelationshipTimelineService relationshipTimelineService;

    @GetMapping("/api/relationships/{relationshipId}/timeline")
    public Result<List<RelationshipTimelineEventResponse>> listTimelineEvents(
            @PathVariable Long relationshipId,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String importance,
            @RequestParam(defaultValue = "ASC") String order,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(relationshipTimelineService.listTimelineEvents(relationshipId, eventType, importance, order, loginUser.getId()));
    }

    @GetMapping("/api/relationships/{relationshipId}/timeline/{eventId}")
    public Result<RelationshipTimelineEventResponse> getTimelineEventDetail(
            @PathVariable Long relationshipId,
            @PathVariable Long eventId,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(relationshipTimelineService.getTimelineEventDetail(relationshipId, eventId, loginUser.getId()));
    }

    @PostMapping("/api/relationships/{relationshipId}/timeline")
    public Result<RelationshipTimelineEventResponse> createCustomEvent(
            @PathVariable Long relationshipId,
            @Valid @RequestBody CreateCustomTimelineEventRequest request,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(relationshipTimelineService.createCustomEvent(relationshipId, request, loginUser.getId()));
    }

    @DeleteMapping("/api/relationships/{relationshipId}/timeline/{eventId}")
    public Result<Void> deleteTimelineEvent(
            @PathVariable Long relationshipId,
            @PathVariable Long eventId,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        relationshipTimelineService.deleteTimelineEvent(relationshipId, eventId, loginUser.getId());
        return Result.success();
    }
}
