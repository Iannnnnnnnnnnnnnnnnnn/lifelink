package com.lifelink.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserProfileRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username length must be between 3 and 50")
    private String username;

    @Email(message = "Email format is invalid")
    @Size(max = 100, message = "Email length must be at most 100")
    private String email;

    @Size(max = 30, message = "Phone length must be at most 30")
    private String phone;
}
