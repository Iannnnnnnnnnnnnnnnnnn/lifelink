package com.lifelink.cycle.service;

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

@Service
@RequiredArgsConstructor
public class CycleCareAccessService {

    public static final String ACCESS_DENIED_MESSAGE = "生理期关怀功能仅对恋人关系空间开放";

    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String COUPLE_TYPE = "COUPLE";

    private final RelationshipMapper relationshipMapper;
    private final RelationshipMemberMapper relationshipMemberMapper;

    public boolean canAccess(Long userId) {
        return !getAccessibleLoverSpaceIds(userId).isEmpty();
    }

    public void requireAccess(Long userId) {
        if (!canAccess(userId)) {
            throw new BusinessException(403, ACCESS_DENIED_MESSAGE);
        }
    }

    public boolean hasActiveLoverSpace(Long userId) {
        return canAccess(userId);
    }

    public List<Long> getAccessibleLoverSpaceIds(Long userId) {
        if (userId == null) {
            return new ArrayList<Long>();
        }
        List<RelationshipMember> memberships = relationshipMemberMapper.selectList(new LambdaQueryWrapper<RelationshipMember>()
                .eq(RelationshipMember::getUserId, userId)
                .eq(RelationshipMember::getStatus, ACTIVE_STATUS));
        List<Long> result = new ArrayList<Long>();
        for (RelationshipMember membership : memberships) {
            Relationship relationship = relationshipMapper.selectById(membership.getRelationshipId());
            if (relationship != null
                    && ACTIVE_STATUS.equals(relationship.getStatus())
                    && COUPLE_TYPE.equals(relationship.getType())) {
                result.add(relationship.getId());
            }
        }
        return result;
    }

    public Long resolveLoverSpaceId(Long userId, Long requestedSpaceId) {
        List<Long> accessibleIds = getAccessibleLoverSpaceIds(userId);
        if (accessibleIds.isEmpty()) {
            throw new BusinessException(403, ACCESS_DENIED_MESSAGE);
        }
        if (requestedSpaceId == null) {
            return accessibleIds.get(0);
        }
        if (!accessibleIds.contains(requestedSpaceId)) {
            throw new BusinessException(403, ACCESS_DENIED_MESSAGE);
        }
        return requestedSpaceId;
    }

    public void requireActiveLoverSpaceMember(Long loverSpaceId, Long userId) {
        if (loverSpaceId == null || !getAccessibleLoverSpaceIds(userId).contains(loverSpaceId)) {
            throw new BusinessException(403, ACCESS_DENIED_MESSAGE);
        }
    }
}
