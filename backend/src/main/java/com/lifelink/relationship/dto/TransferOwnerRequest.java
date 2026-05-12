package com.lifelink.relationship.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransferOwnerRequest {

    @NotNull(message = "Target user is required")
    private Long targetUserId;
}
