package com.lifelink.relationship.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifelink.common.BusinessException;
import com.lifelink.relationship.entity.Relationship;
import com.lifelink.relationship.entity.RelationshipMember;
import com.lifelink.relationship.mapper.RelationshipMapper;
import com.lifelink.relationship.mapper.RelationshipMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RelationshipPermissionService {

    public static final String ACTIVE_STATUS = "ACTIVE";
    public static final String OWNER_ROLE = "OWNER";
    public static final String ADMIN_ROLE = "ADMIN";

    private final RelationshipMapper relationshipMapper;
    private final RelationshipMemberMapper relationshipMemberMapper;

    public Relationship requireActiveRelationship(Long relationshipId) {
        Relationship relationship = relationshipMapper.selectById(relationshipId);
        if (relationship == null || !ACTIVE_STATUS.equals(relationship.getStatus())) {
            throw new BusinessException(404, "Relationship not found");
        }
        return relationship;
    }

    public RelationshipMember requireActiveRelationshipMember(Long relationshipId, Long userId) {
        requireActiveRelationship(relationshipId);
        RelationshipMember member = relationshipMemberMapper.selectOne(new LambdaQueryWrapper<RelationshipMember>()
                .eq(RelationshipMember::getRelationshipId, relationshipId)
                .eq(RelationshipMember::getUserId, userId)
                .eq(RelationshipMember::getStatus, ACTIVE_STATUS)
                .last("LIMIT 1"));
        if (member == null) {
            throw new BusinessException(403, "No permission to access this relationship");
        }
        return member;
    }

    public RelationshipMember requireRelationshipOwner(Long relationshipId, Long userId) {
        RelationshipMember member = requireActiveRelationshipMember(relationshipId, userId);
        if (!OWNER_ROLE.equals(member.getRole())) {
            throw new BusinessException(403, "Only owner can operate this relationship");
        }
        return member;
    }

    public RelationshipMember requireRelationshipAdminOrOwner(Long relationshipId, Long userId) {
        RelationshipMember member = requireActiveRelationshipMember(relationshipId, userId);
        if (!OWNER_ROLE.equals(member.getRole()) && !ADMIN_ROLE.equals(member.getRole())) {
            throw new BusinessException(403, "Only owner or admin can operate this relationship");
        }
        return member;
    }

    public boolean isActiveRelationshipMember(Long relationshipId, Long userId) {
        try {
            requireActiveRelationshipMember(relationshipId, userId);
            return true;
        } catch (BusinessException ex) {
            return false;
        }
    }

    public List<RelationshipMember> listActiveMembers(Long relationshipId) {
        requireActiveRelationship(relationshipId);
        return relationshipMemberMapper.selectList(new LambdaQueryWrapper<RelationshipMember>()
                .eq(RelationshipMember::getRelationshipId, relationshipId)
                .eq(RelationshipMember::getStatus, ACTIVE_STATUS));
    }

    public List<Long> listActiveRelationshipIds(Long userId) {
        List<RelationshipMember> members = relationshipMemberMapper.selectList(new LambdaQueryWrapper<RelationshipMember>()
                .eq(RelationshipMember::getUserId, userId)
                .eq(RelationshipMember::getStatus, ACTIVE_STATUS));
        if (members.isEmpty()) {
            return new ArrayList<Long>();
        }
        List<Long> relationshipIds = members.stream().map(RelationshipMember::getRelationshipId).collect(Collectors.toList());
        List<Relationship> relationships = relationshipMapper.selectList(new LambdaQueryWrapper<Relationship>()
                .in(Relationship::getId, relationshipIds)
                .eq(Relationship::getStatus, ACTIVE_STATUS));
        return relationships.stream().map(Relationship::getId).collect(Collectors.toList());
    }
}
