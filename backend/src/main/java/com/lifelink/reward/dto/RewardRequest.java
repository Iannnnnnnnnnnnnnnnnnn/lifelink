package com.lifelink.reward.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RewardRequest {

    @NotBlank(message = "Reward title is required")
    @Size(max = 120, message = "Reward title cannot exceed 120 characters")
    private String title;

    @Size(max = 1000, message = "Reward description cannot exceed 1000 characters")
    private String description;

    private String coverObjectKey;

    private String coverUrl;

    @NotNull(message = "Required coins is required")
    @Min(value = 1, message = "Required coins must be greater than 0")
    private Integer coinCost;

    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    private String status;

    private Integer sortOrder;
}
