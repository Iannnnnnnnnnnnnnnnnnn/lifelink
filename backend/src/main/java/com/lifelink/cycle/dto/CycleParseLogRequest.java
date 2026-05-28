package com.lifelink.cycle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CycleParseLogRequest {

    @NotBlank(message = "Text is required")
    @Size(max = 1000, message = "Text length must be at most 1000")
    private String text;
}
