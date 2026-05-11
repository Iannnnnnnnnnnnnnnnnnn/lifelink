package com.lifelink.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;

    private String username;

    private String email;

    private String phone;

    private String avatarUrl;

    private String status;

    private LocalDateTime createdAt;
}
