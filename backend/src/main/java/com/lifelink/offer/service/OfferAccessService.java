package com.lifelink.offer.service;

import com.lifelink.offer.dto.OfferAccessResponse;
import com.lifelink.relationship.entity.RelationshipMember;
import com.lifelink.relationship.service.RelationshipPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OfferAccessService {

    public static final long OFFER_RELATIONSHIP_ID = 1L;

    private final RelationshipPermissionService relationshipPermissionService;

    public OfferAccessResponse getAccess(Long userId) {
        RelationshipMember member = getMember(userId);
        return new OfferAccessResponse(isManager(member));
    }

    public void requireMember(Long userId) {
        relationshipPermissionService.requireActiveRelationshipMember(OFFER_RELATIONSHIP_ID, userId);
    }

    public void requireManager(Long userId) {
        relationshipPermissionService.requireRelationshipAdminOrOwner(OFFER_RELATIONSHIP_ID, userId);
    }

    private RelationshipMember getMember(Long userId) {
        return relationshipPermissionService.requireActiveRelationshipMember(OFFER_RELATIONSHIP_ID, userId);
    }

    private boolean isManager(RelationshipMember member) {
        return RelationshipPermissionService.OWNER_ROLE.equals(member.getRole())
                || RelationshipPermissionService.ADMIN_ROLE.equals(member.getRole());
    }
}
