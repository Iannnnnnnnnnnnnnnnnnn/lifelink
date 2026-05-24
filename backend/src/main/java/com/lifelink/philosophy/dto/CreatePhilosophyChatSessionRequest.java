package com.lifelink.philosophy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePhilosophyChatSessionRequest {

    @NotBlank(message = "Philosopher code is required")
    private String philosopherCode;

    private String language;
}
