package com.lifelink.relationship.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelationshipMemberResponse {

    private Long userId;

    private String username;

    private String avatarUrl;

    private String role;

    private String nickname;

    private LocalDateTime joinedAt;
}
