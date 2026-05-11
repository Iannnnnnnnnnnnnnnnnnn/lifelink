package com.lifelink.relationship.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateRelationshipRequest {

    @NotBlank(message = "Relationship name is required")
    @Size(max = 100, message = "Relationship name length must be at most 100")
    private String name;

    @NotBlank(message = "Relationship type is required")
    @Pattern(regexp = "COUPLE|FAMILY|FRIEND|ROOMMATE|CUSTOM", message = "Relationship type is invalid")
    private String type;

    @Size(max = 500, message = "Description length must be at most 500")
    private String description;
}
