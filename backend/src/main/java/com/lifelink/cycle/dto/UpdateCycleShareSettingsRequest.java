package com.lifelink.cycle.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateCycleShareSettingsRequest {

    @NotBlank(message = "Share level is required")
    private String shareLevel;
}
