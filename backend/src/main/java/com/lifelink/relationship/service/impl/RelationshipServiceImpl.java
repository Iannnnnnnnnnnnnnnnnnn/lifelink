package com.lifelink.relationship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifelink.activity.service.SpaceActivityService;
import com.lifelink.common.BusinessException;
import com.lifelink.relationship.dto.CreateInviteResponse;
import com.lifelink.relationship.dto.CreateRelationshipRequest;
import com.lifelink.relationship.dto.JoinRelationshipRequest;
import com.lifelink.relationship.dto.RelationshipDetailResponse;
import com.lifelink.relationship.dto.RelationshipMemberResponse;
import com.lifelink.relationship.dto.RelationshipResponse;
import com.lifelink.relationship.entity.Relationship;
import com.lifelink.relationship.entity.RelationshipInvite;
import com.lifelink.relationship.entity.RelationshipMember;
import com.lifelink.relationship.mapper.RelationshipInviteMapper;
import com.lifelink.relationship.mapper.RelationshipMapper;
import com.lifelink.relationship.mapper.RelationshipMemberMapper;
import com.lifelink.relationship.service.RelationshipService;
import com.lifelink.user.entity.User;
import com.lifelink.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RelationshipServiceImpl implements RelationshipService {

    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String OWNER_ROLE = "OWNER";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String MEMBER_ROLE = "MEMBER";
    private static final String INVITE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final RelationshipMapper relationshipMapper;
    private final RelationshipMemberMapper relationshipMemberMapper;
    private final RelationshipInviteMapper relationshipInviteMapper;
    private final UserMapper userMapper;
    private final SpaceActivityService spaceActivityService;

    @Override
    @Transactional
    public RelationshipDetailResponse createRelationship(CreateRelationshipRequest request, Long userId) {
        LocalDateTime now = LocalDateTime.now();

        Relationship relationship = new Relationship();
        relationship.setName(request.getName().trim());
        relationship.setType(request.getType());
        relationship.setDescription(request.getDescription());
        relationship.setOwnerId(userId);
        relationship.setStatus(ACTIVE_STATUS);
        relationship.setCreatedAt(now);
        relationship.setUpdatedAt(now);
        relationshipMapper.insert(relationship);

        RelationshipMember owner = new RelationshipMember();
        owner.setRelationshipId(relationship.getId());
        owner.setUserId(userId);
        owner.setRole(OWNER_ROLE);
        owner.setJoinedAt(now);
        relationshipMemberMapper.insert(owner);
        createActivitySafely(
                relationship.getId(),
                userId,
                "RELATIONSHIP_CREATED",
                "RELATIONSHIP",
                relationship.getId(),
                "Created relationship space: " + relationship.getName(),
                null,
                Map.of("relationshipName", relationship.getName(), "relationshipType", relationship.getType())
        );

        return toDetail(relationship, OWNER_ROLE);
    }

    @Override
    public List<RelationshipResponse> listRelationships(Long userId) {
        List<RelationshipMember> memberships = relationshipMemberMapper.selectList(new LambdaQueryWrapper<RelationshipMember>()
                .eq(RelationshipMember::getUserId, userId));
        List<RelationshipResponse> responses = new ArrayList<RelationshipResponse>();
        for (RelationshipMember member : memberships) {
            Relationship relationship = relationshipMapper.selectById(member.getRelationshipId());
            if (relationship != null && ACTIVE_STATUS.equals(relationship.getStatus())) {
                responses.add(toResponse(relationship, member.getRole()));
            }
        }
        return responses;
    }

    @Override
    public RelationshipDetailResponse getRelationship(Long relationshipId, Long userId) {
        RelationshipMember member = requireMember(relationshipId, userId);
        Relationship relationship = requireActiveRelationship(relationshipId);
        return toDetail(relationship, member.getRole());
    }

    @Override
    public List<RelationshipMemberResponse> listMembers(Long relationshipId, Long userId) {
        requireMember(relationshipId, userId);
        requireActiveRelationship(relationshipId);

        List<RelationshipMember> members = relationshipMemberMapper.selectList(new LambdaQueryWrapper<RelationshipMember>()
                .eq(RelationshipMember::getRelationshipId, relationshipId));
        List<RelationshipMemberResponse> responses = new ArrayList<RelationshipMemberResponse>();
        for (RelationshipMember member : members) {
            User user = userMapper.selectById(member.getUserId());
            if (user != null) {
                responses.add(new RelationshipMemberResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getAvatarUrl(),
                        member.getRole(),
                        member.getNickname(),
                        member.getJoinedAt()
                ));
            }
        }
        return responses;
    }

    @Override
    @Transactional
    public CreateInviteResponse createInvite(Long relationshipId, Long userId) {
        RelationshipMember member = requireMember(relationshipId, userId);
        requireActiveRelationship(relationshipId);
        if (!OWNER_ROLE.equals(member.getRole()) && !ADMIN_ROLE.equals(member.getRole())) {
            throw new BusinessException(403, "Only owner or admin can create invite code");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireAt = now.plusDays(7);
        RelationshipInvite invite = new RelationshipInvite();
        invite.setRelationshipId(relationshipId);
        invite.setInviterId(userId);
        invite.setInviteCode(generateUniqueInviteCode());
        invite.setStatus(ACTIVE_STATUS);
        invite.setExpireAt(expireAt);
        invite.setCreatedAt(now);
        relationshipInviteMapper.insert(invite);

        return new CreateInviteResponse(invite.getInviteCode(), expireAt);
    }

    @Override
    @Transactional
    public RelationshipDetailResponse joinRelationship(JoinRelationshipRequest request, Long userId) {
        RelationshipInvite invite = relationshipInviteMapper.selectOne(new LambdaQueryWrapper<RelationshipInvite>()
                .eq(RelationshipInvite::getInviteCode, request.getInviteCode().trim().toUpperCase())
                .last("LIMIT 1"));
        if (invite == null || !ACTIVE_STATUS.equals(invite.getStatus())) {
            throw new BusinessException(404, "Invite code not found");
        }
        if (invite.getExpireAt() != null && invite.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(400, "Invite code has expired");
        }

        Relationship relationship = requireActiveRelationship(invite.getRelationshipId());
        RelationshipMember existing = findMember(invite.getRelationshipId(), userId);
        if (existing != null) {
            throw new BusinessException(400, "You are already a member of this relationship");
        }

        RelationshipMember member = new RelationshipMember();
        member.setRelationshipId(invite.getRelationshipId());
        member.setUserId(userId);
        member.setRole(MEMBER_ROLE);
        member.setJoinedAt(LocalDateTime.now());
        relationshipMemberMapper.insert(member);
        User user = userMapper.selectById(userId);
        String username = user == null ? null : user.getUsername();
        createActivitySafely(
                relationship.getId(),
                userId,
                "MEMBER_JOINED",
                "USER",
                userId,
                (username == null ? "A member" : username) + " joined the space",
                null,
                Map.of("username", username == null ? "" : username)
        );

        return toDetail(relationship, MEMBER_ROLE);
    }

    private void createActivitySafely(Long relationshipId, Long actorUserId, String activityType, String targetType, Long targetId, String title, String content, Map<String, Object> metadata) {
        try {
            spaceActivityService.createActivity(relationshipId, actorUserId, activityType, targetType, targetId, title, content, metadata);
        } catch (Exception ex) {
            log.warn("Create relationship activity failed: {}", activityType, ex);
        }
    }

    private Relationship requireActiveRelationship(Long relationshipId) {
        Relationship relationship = relationshipMapper.selectById(relationshipId);
        if (relationship == null || !ACTIVE_STATUS.equals(relationship.getStatus())) {
            throw new BusinessException(404, "Relationship not found");
        }
        return relationship;
    }

    private RelationshipMember requireMember(Long relationshipId, Long userId) {
        RelationshipMember member = findMember(relationshipId, userId);
        if (member == null) {
            throw new BusinessException(403, "You are not a member of this relationship");
        }
        return member;
    }

    private RelationshipMember findMember(Long relationshipId, Long userId) {
        return relationshipMemberMapper.selectOne(new LambdaQueryWrapper<RelationshipMember>()
                .eq(RelationshipMember::getRelationshipId, relationshipId)
                .eq(RelationshipMember::getUserId, userId)
                .last("LIMIT 1"));
    }

    private RelationshipResponse toResponse(Relationship relationship, String role) {
        return new RelationshipResponse(
                relationship.getId(),
                relationship.getName(),
                relationship.getType(),
                relationship.getDescription(),
                relationship.getOwnerId(),
                relationship.getStatus(),
                role,
                relationship.getCreatedAt()
        );
    }

    private RelationshipDetailResponse toDetail(Relationship relationship, String role) {
        return new RelationshipDetailResponse(
                relationship.getId(),
                relationship.getName(),
                relationship.getType(),
                relationship.getDescription(),
                relationship.getOwnerId(),
                relationship.getStatus(),
                role,
                relationship.getCreatedAt(),
                relationship.getUpdatedAt()
        );
    }

    private String generateUniqueInviteCode() {
        String code;
        do {
            code = generateInviteCode();
        } while (relationshipInviteMapper.selectCount(new LambdaQueryWrapper<RelationshipInvite>()
                .eq(RelationshipInvite::getInviteCode, code)) > 0);
        return code;
    }

    private String generateInviteCode() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            builder.append(INVITE_CHARS.charAt(RANDOM.nextInt(INVITE_CHARS.length())));
        }
        return builder.toString();
    }
}
