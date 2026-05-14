package com.lifelink.relationship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifelink.activity.service.SpaceActivityService;
import com.lifelink.common.BusinessException;
import com.lifelink.notification.service.NotificationService;
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
import com.lifelink.relationship.service.RelationshipPermissionService;
import com.lifelink.relationship.service.RelationshipService;
import com.lifelink.timeline.service.RelationshipTimelineService;
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
    private static final String DELETED_STATUS = "DELETED";
    private static final String LEFT_STATUS = "LEFT";
    private static final String REMOVED_STATUS = "REMOVED";
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
    private final NotificationService notificationService;
    private final RelationshipPermissionService relationshipPermissionService;
    private final RelationshipTimelineService relationshipTimelineService;

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
        owner.setStatus(ACTIVE_STATUS);
        owner.setJoinedAt(now);
        owner.setUpdatedAt(now);
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
        createTimelineEventSafely(
                relationship.getId(),
                "RELATIONSHIP_CREATED",
                "Created relationship space",
                "Relationship space \"" + relationship.getName() + "\" started recording",
                userId,
                "RELATIONSHIP",
                relationship.getId(),
                null,
                null,
                relationship.getCreatedAt(),
                "IMPORTANT",
                Map.of("relationshipName", relationship.getName(), "relationshipType", relationship.getType())
        );

        return toDetail(relationship, OWNER_ROLE);
    }

    @Override
    public List<RelationshipResponse> listRelationships(Long userId) {
        List<RelationshipMember> memberships = relationshipMemberMapper.selectList(new LambdaQueryWrapper<RelationshipMember>()
                .eq(RelationshipMember::getUserId, userId)
                .eq(RelationshipMember::getStatus, ACTIVE_STATUS));
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
                .eq(RelationshipMember::getRelationshipId, relationshipId)
                .eq(RelationshipMember::getStatus, ACTIVE_STATUS));
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
        relationshipPermissionService.requireRelationshipAdminOrOwner(relationshipId, userId);

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
        LocalDateTime now = LocalDateTime.now();
        RelationshipMember existing = findAnyMember(invite.getRelationshipId(), userId);
        if (existing != null && ACTIVE_STATUS.equals(existing.getStatus())) {
            throw new BusinessException(400, "You are already a member of this relationship");
        }

        if (existing == null) {
            RelationshipMember member = new RelationshipMember();
            member.setRelationshipId(invite.getRelationshipId());
            member.setUserId(userId);
            member.setRole(MEMBER_ROLE);
            member.setStatus(ACTIVE_STATUS);
            member.setJoinedAt(now);
            member.setUpdatedAt(now);
            relationshipMemberMapper.insert(member);
        } else {
            existing.setRole(MEMBER_ROLE);
            existing.setNickname(null);
            existing.setStatus(ACTIVE_STATUS);
            existing.setJoinedAt(now);
            existing.setUpdatedAt(now);
            relationshipMemberMapper.updateById(existing);
        }
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
        createTimelineEventSafely(
                relationship.getId(),
                "MEMBER_JOINED",
                "New member joined",
                (username == null ? "A member" : username) + " joined the relationship space",
                userId,
                "USER",
                userId,
                null,
                null,
                now,
                "NORMAL",
                Map.of("username", username == null ? "" : username)
        );
        createNotificationSafely(
                relationship.getOwnerId(),
                userId,
                "RELATIONSHIP_MEMBER_JOINED",
                "Someone joined your relationship space",
                (username == null ? "Someone" : username) + " joined " + relationship.getName(),
                "RELATIONSHIP",
                relationship.getId(),
                relationship.getId(),
                Map.of("relationshipName", relationship.getName(), "username", username == null ? "" : username)
        );

        return toDetail(relationship, MEMBER_ROLE);
    }

    @Override
    @Transactional
    public void updateMyNickname(Long relationshipId, com.lifelink.relationship.dto.UpdateMyNicknameRequest request, Long userId) {
        requireActiveRelationship(relationshipId);
        RelationshipMember member = requireMember(relationshipId, userId);
        String nickname = request.getNickname();
        member.setNickname(nickname == null || nickname.trim().isEmpty() ? null : nickname.trim());
        member.setUpdatedAt(LocalDateTime.now());
        relationshipMemberMapper.updateById(member);
    }

    @Override
    @Transactional
    public void leaveRelationship(Long relationshipId, Long userId) {
        Relationship relationship = requireActiveRelationship(relationshipId);
        RelationshipMember member = requireMember(relationshipId, userId);
        long activeMemberCount = countActiveMembers(relationshipId);
        if (OWNER_ROLE.equals(member.getRole()) && activeMemberCount > 1) {
            throw new BusinessException(400, "Owner must transfer ownership or dissolve the relationship before leaving");
        }

        LocalDateTime now = LocalDateTime.now();
        member.setStatus(LEFT_STATUS);
        member.setUpdatedAt(now);
        relationshipMemberMapper.updateById(member);
        if (OWNER_ROLE.equals(member.getRole())) {
            relationship.setStatus(DELETED_STATUS);
            relationship.setUpdatedAt(now);
            relationshipMapper.updateById(relationship);
        }

        createActivitySafely(
                relationshipId,
                userId,
                "MEMBER_LEFT",
                "USER",
                userId,
                "A member left the space",
                null,
                Map.of("username", getUsername(userId))
        );
    }

    @Override
    @Transactional
    public void dissolveRelationship(Long relationshipId, Long userId) {
        Relationship relationship = requireActiveRelationship(relationshipId);
        requireOwner(relationshipId, userId);
        List<RelationshipMember> members = listActiveMembers(relationshipId);

        LocalDateTime now = LocalDateTime.now();
        relationship.setStatus(DELETED_STATUS);
        relationship.setUpdatedAt(now);
        relationshipMapper.updateById(relationship);
        for (RelationshipMember member : members) {
            member.setStatus(REMOVED_STATUS);
            member.setUpdatedAt(now);
            relationshipMemberMapper.updateById(member);
            if (!userId.equals(member.getUserId())) {
                createNotificationSafely(
                        member.getUserId(),
                        userId,
                        "RELATIONSHIP_DELETED",
                        "Relationship space dissolved",
                        relationship.getName() + " has been dissolved",
                        "RELATIONSHIP",
                        relationshipId,
                        relationshipId,
                        Map.of("relationshipName", relationship.getName())
                );
            }
        }

        createActivitySafely(
                relationshipId,
                userId,
                "RELATIONSHIP_DELETED",
                "RELATIONSHIP",
                relationshipId,
                "Dissolved relationship space: " + relationship.getName(),
                null,
                Map.of("relationshipName", relationship.getName())
        );
    }

    @Override
    @Transactional
    public void updateMemberRole(Long relationshipId, Long targetUserId, com.lifelink.relationship.dto.UpdateMemberRoleRequest request, Long userId) {
        Relationship relationship = requireActiveRelationship(relationshipId);
        requireOwner(relationshipId, userId);
        RelationshipMember target = requireMember(relationshipId, targetUserId);
        if (OWNER_ROLE.equals(target.getRole())) {
            throw new BusinessException(400, "Cannot change owner role here");
        }
        String role = request.getRole();
        if (!ADMIN_ROLE.equals(role) && !MEMBER_ROLE.equals(role)) {
            throw new BusinessException(400, "Role must be ADMIN or MEMBER");
        }

        target.setRole(role);
        target.setUpdatedAt(LocalDateTime.now());
        relationshipMemberMapper.updateById(target);
        createActivitySafely(
                relationshipId,
                userId,
                "MEMBER_ROLE_UPDATED",
                "USER",
                targetUserId,
                "Updated member role",
                null,
                Map.of("username", getUsername(targetUserId), "role", role)
        );
        if (ADMIN_ROLE.equals(role)) {
            createNotificationSafely(
                    targetUserId,
                    userId,
                    "MEMBER_ROLE_UPDATED",
                    "You were set as admin",
                    "You were set as admin in " + relationship.getName(),
                    "RELATIONSHIP",
                    relationshipId,
                    relationshipId,
                    Map.of("relationshipName", relationship.getName(), "role", role)
            );
        }
    }

    @Override
    @Transactional
    public void removeMember(Long relationshipId, Long targetUserId, Long userId) {
        Relationship relationship = requireActiveRelationship(relationshipId);
        requireOwner(relationshipId, userId);
        RelationshipMember target = requireMember(relationshipId, targetUserId);
        if (OWNER_ROLE.equals(target.getRole())) {
            throw new BusinessException(400, "Cannot remove owner");
        }

        target.setStatus(REMOVED_STATUS);
        target.setUpdatedAt(LocalDateTime.now());
        relationshipMemberMapper.updateById(target);
        createActivitySafely(
                relationshipId,
                userId,
                "MEMBER_REMOVED",
                "USER",
                targetUserId,
                "Removed a member",
                null,
                Map.of("username", getUsername(targetUserId))
        );
        createNotificationSafely(
                targetUserId,
                userId,
                "MEMBER_REMOVED",
                "You were removed from a relationship space",
                "You were removed from " + relationship.getName(),
                "RELATIONSHIP",
                relationshipId,
                relationshipId,
                Map.of("relationshipName", relationship.getName())
        );
    }

    @Override
    @Transactional
    public void transferOwner(Long relationshipId, com.lifelink.relationship.dto.TransferOwnerRequest request, Long userId) {
        Relationship relationship = requireActiveRelationship(relationshipId);
        requireOwner(relationshipId, userId);
        Long targetUserId = request.getTargetUserId();
        if (userId.equals(targetUserId)) {
            throw new BusinessException(400, "Target user is already owner");
        }
        RelationshipMember target = requireMember(relationshipId, targetUserId);

        LocalDateTime now = LocalDateTime.now();
        for (RelationshipMember activeMember : listActiveMembers(relationshipId)) {
            if (OWNER_ROLE.equals(activeMember.getRole()) && !targetUserId.equals(activeMember.getUserId())) {
                activeMember.setRole(ADMIN_ROLE);
                activeMember.setUpdatedAt(now);
                relationshipMemberMapper.updateById(activeMember);
            }
        }
        target.setRole(OWNER_ROLE);
        target.setUpdatedAt(now);
        relationshipMemberMapper.updateById(target);
        relationship.setOwnerId(targetUserId);
        relationship.setUpdatedAt(now);
        relationshipMapper.updateById(relationship);

        createActivitySafely(
                relationshipId,
                userId,
                "OWNER_TRANSFERRED",
                "USER",
                targetUserId,
                "Transferred relationship owner",
                null,
                Map.of("username", getUsername(targetUserId))
        );
        createNotificationSafely(
                targetUserId,
                userId,
                "OWNER_TRANSFERRED",
                "You are now the owner",
                "You are now the owner of " + relationship.getName(),
                "RELATIONSHIP",
                relationshipId,
                relationshipId,
                Map.of("relationshipName", relationship.getName())
        );
    }

    private void createActivitySafely(Long relationshipId, Long actorUserId, String activityType, String targetType, Long targetId, String title, String content, Map<String, Object> metadata) {
        try {
            spaceActivityService.createActivity(relationshipId, actorUserId, activityType, targetType, targetId, title, content, metadata);
        } catch (Exception ex) {
            log.warn("Create relationship activity failed: {}", activityType, ex);
        }
    }

    private void createNotificationSafely(Long receiverUserId, Long actorUserId, String notificationType, String title, String content, String relatedType, Long relatedId, Long relationshipId, Map<String, Object> metadata) {
        try {
            notificationService.createNotification(receiverUserId, actorUserId, notificationType, title, content, relatedType, relatedId, relationshipId, metadata);
        } catch (Exception ex) {
            log.warn("Create relationship notification failed: {}", notificationType, ex);
        }
    }

    private void createTimelineEventSafely(Long relationshipId, String eventType, String title, String description, Long actorUserId,
                                           String targetType, Long targetId, Long coverFileId, String coverUrl, LocalDateTime eventDate,
                                           String importance, Map<String, Object> metadata) {
        try {
            relationshipTimelineService.createAutoEvent(relationshipId, eventType, title, description, actorUserId, targetType, targetId,
                    coverFileId, coverUrl, eventDate, importance, metadata);
        } catch (Exception ex) {
            log.warn("Create relationship timeline event failed: {}", eventType, ex);
        }
    }

    private Relationship requireActiveRelationship(Long relationshipId) {
        return relationshipPermissionService.requireActiveRelationship(relationshipId);
    }

    private RelationshipMember requireMember(Long relationshipId, Long userId) {
        return relationshipPermissionService.requireActiveRelationshipMember(relationshipId, userId);
    }

    private RelationshipMember requireOwner(Long relationshipId, Long userId) {
        return relationshipPermissionService.requireRelationshipOwner(relationshipId, userId);
    }

    private RelationshipMember findMember(Long relationshipId, Long userId) {
        return relationshipMemberMapper.selectOne(new LambdaQueryWrapper<RelationshipMember>()
                .eq(RelationshipMember::getRelationshipId, relationshipId)
                .eq(RelationshipMember::getUserId, userId)
                .eq(RelationshipMember::getStatus, ACTIVE_STATUS)
                .last("LIMIT 1"));
    }

    private RelationshipMember findAnyMember(Long relationshipId, Long userId) {
        return relationshipMemberMapper.selectOne(new LambdaQueryWrapper<RelationshipMember>()
                .eq(RelationshipMember::getRelationshipId, relationshipId)
                .eq(RelationshipMember::getUserId, userId)
                .last("LIMIT 1"));
    }

    private long countActiveMembers(Long relationshipId) {
        return relationshipPermissionService.listActiveMembers(relationshipId).size();
    }

    private List<RelationshipMember> listActiveMembers(Long relationshipId) {
        return relationshipPermissionService.listActiveMembers(relationshipId);
    }

    private String getUsername(Long userId) {
        User user = userMapper.selectById(userId);
        return user == null ? "" : user.getUsername();
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
