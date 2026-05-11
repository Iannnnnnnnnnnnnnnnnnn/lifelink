package com.lifelink.anniversary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateAnniversaryRequest {

    @NotNull(message = "Relationship is required")
    private Long relationshipId;

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title length must be at most 100")
    private String title;

    private String description;

    @NotNull(message = "Anniversary date is required")
    private LocalDate anniversaryDate;

    @Pattern(regexp = "NONE|YEARLY", message = "Repeat type is invalid")
    private String repeatType;

    private Long backgroundFileId;
}
