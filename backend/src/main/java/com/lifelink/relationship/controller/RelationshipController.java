package com.lifelink.relationship.controller;

import com.lifelink.common.Result;
import com.lifelink.relationship.dto.CreateInviteResponse;
import com.lifelink.relationship.dto.CreateRelationshipRequest;
import com.lifelink.relationship.dto.JoinRelationshipRequest;
import com.lifelink.relationship.dto.RelationshipDetailResponse;
import com.lifelink.relationship.dto.RelationshipMemberResponse;
import com.lifelink.relationship.dto.RelationshipResponse;
import com.lifelink.relationship.dto.TransferOwnerRequest;
import com.lifelink.relationship.dto.UpdateMemberRoleRequest;
import com.lifelink.relationship.dto.UpdateMyNicknameRequest;
import com.lifelink.relationship.service.RelationshipService;
import com.lifelink.security.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/relationships")
@RequiredArgsConstructor
public class RelationshipController {

    private final RelationshipService relationshipService;

    @PostMapping
    public Result<RelationshipDetailResponse> createRelationship(
            @Valid @RequestBody CreateRelationshipRequest request,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(relationshipService.createRelationship(request, loginUser.getId()));
    }

    @GetMapping
    public Result<List<RelationshipResponse>> listRelationships(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(relationshipService.listRelationships(loginUser.getId()));
    }

    @GetMapping("/{id}")
    public Result<RelationshipDetailResponse> getRelationship(
            @PathVariable Long id,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(relationshipService.getRelationship(id, loginUser.getId()));
    }

    @GetMapping("/{id}/members")
    public Result<List<RelationshipMemberResponse>> listMembers(
            @PathVariable Long id,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(relationshipService.listMembers(id, loginUser.getId()));
    }

    @PostMapping("/{id}/invite")
    public Result<CreateInviteResponse> createInvite(
            @PathVariable Long id,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(relationshipService.createInvite(id, loginUser.getId()));
    }

    @PostMapping("/join")
    public Result<RelationshipDetailResponse> joinRelationship(
            @Valid @RequestBody JoinRelationshipRequest request,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(relationshipService.joinRelationship(request, loginUser.getId()));
    }

    @PatchMapping("/{id}/members/me/nickname")
    public Result<Void> updateMyNickname(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMyNicknameRequest request,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        relationshipService.updateMyNickname(id, request, loginUser.getId());
        return Result.success();
    }

    @PostMapping("/{id}/leave")
    public Result<Void> leaveRelationship(
            @PathVariable Long id,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        relationshipService.leaveRelationship(id, loginUser.getId());
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> dissolveRelationship(
            @PathVariable Long id,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        relationshipService.dissolveRelationship(id, loginUser.getId());
        return Result.success();
    }

    @PatchMapping("/{id}/members/{userId}/role")
    public Result<Void> updateMemberRole(
            @PathVariable Long id,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateMemberRoleRequest request,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        relationshipService.updateMemberRole(id, userId, request, loginUser.getId());
        return Result.success();
    }

    @DeleteMapping("/{id}/members/{userId}")
    public Result<Void> removeMember(
            @PathVariable Long id,
            @PathVariable Long userId,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        relationshipService.removeMember(id, userId, loginUser.getId());
        return Result.success();
    }

    @PostMapping("/{id}/transfer-owner")
    public Result<Void> transferOwner(
            @PathVariable Long id,
            @Valid @RequestBody TransferOwnerRequest request,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        relationshipService.transferOwner(id, request, loginUser.getId());
        return Result.success();
    }
}
