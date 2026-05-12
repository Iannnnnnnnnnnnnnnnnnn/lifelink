package com.lifelink.relationship.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateMyNicknameRequest {

    @Size(max = 50, message = "Nickname length must be at most 50")
    private String nickname;
}
