package com.lifelink.user.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username length must be between 3 and 50")
    private String username;

    @Email(message = "Email format is invalid")
    @Size(max = 100, message = "Email length must be at most 100")
    private String email;

    @Size(max = 30, message = "Phone length must be at most 30")
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password length must be at least 6")
    private String password;

    @AssertTrue(message = "Email or phone is required")
    public boolean isEmailOrPhoneProvided() {
        return StringUtils.hasText(email) || StringUtils.hasText(phone);
    }
}
