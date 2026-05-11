package com.lifelink.daily.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateDailyPostRequest {

    @NotNull(message = "Relationship id is required")
    private Long relationshipId;

    @NotBlank(message = "Content is required")
    private String content;

    @Size(max = 30, message = "Mood length must be at most 30")
    private String mood;

    @Size(max = 20, message = "Visibility length must be at most 20")
    private String visibility;

    @Size(max = 9, message = "At most 9 images are allowed")
    private List<Long> imageIds;
}
