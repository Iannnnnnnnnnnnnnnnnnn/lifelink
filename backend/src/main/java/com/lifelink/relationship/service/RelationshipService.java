package com.lifelink.relationship.service;

import com.lifelink.relationship.dto.CreateInviteResponse;
import com.lifelink.relationship.dto.CreateRelationshipRequest;
import com.lifelink.relationship.dto.JoinRelationshipRequest;
import com.lifelink.relationship.dto.RelationshipDetailResponse;
import com.lifelink.relationship.dto.RelationshipMemberResponse;
import com.lifelink.relationship.dto.RelationshipResponse;
import com.lifelink.relationship.dto.TransferOwnerRequest;
import com.lifelink.relationship.dto.UpdateMemberRoleRequest;
import com.lifelink.relationship.dto.UpdateMyNicknameRequest;

import java.util.List;

public interface RelationshipService {

    RelationshipDetailResponse createRelationship(CreateRelationshipRequest request, Long userId);

    List<RelationshipResponse> listRelationships(Long userId);

    RelationshipDetailResponse getRelationship(Long relationshipId, Long userId);

    List<RelationshipMemberResponse> listMembers(Long relationshipId, Long userId);

    CreateInviteResponse createInvite(Long relationshipId, Long userId);

    RelationshipDetailResponse joinRelationship(JoinRelationshipRequest request, Long userId);

    void updateMyNickname(Long relationshipId, UpdateMyNicknameRequest request, Long userId);

    void leaveRelationship(Long relationshipId, Long userId);

    void dissolveRelationship(Long relationshipId, Long userId);

    void updateMemberRole(Long relationshipId, Long targetUserId, UpdateMemberRoleRequest request, Long userId);

    void removeMember(Long relationshipId, Long targetUserId, Long userId);

    void transferOwner(Long relationshipId, TransferOwnerRequest request, Long userId);
}
