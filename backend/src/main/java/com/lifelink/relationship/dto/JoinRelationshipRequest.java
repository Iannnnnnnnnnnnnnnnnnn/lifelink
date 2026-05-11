package com.lifelink.relationship.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinRelationshipRequest {

    @NotBlank(message = "Invite code is required")
    private String inviteCode;
}
