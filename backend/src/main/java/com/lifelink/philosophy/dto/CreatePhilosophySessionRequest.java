package com.lifelink.philosophy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreatePhilosophySessionRequest {

    @NotBlank(message = "Question is required")
    @Size(min = 1, max = 1000, message = "Question must be between 1 and 1000 characters")
    private String question;

    @NotEmpty(message = "Please select at least one philosopher")
    @Size(min = 1, max = 8, message = "You can select up to 8 philosophers")
    private List<String> philosopherCodes;

    private String language;
}
