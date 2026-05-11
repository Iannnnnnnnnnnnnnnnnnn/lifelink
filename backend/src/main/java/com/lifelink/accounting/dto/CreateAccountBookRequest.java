package com.lifelink.accounting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAccountBookRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name length must be at most 100")
    private String name;

    @NotBlank(message = "Type is required")
    @Size(max = 20, message = "Type length must be at most 20")
    private String type;

    private Long relationshipId;
}
