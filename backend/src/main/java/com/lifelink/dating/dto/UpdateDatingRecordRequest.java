package com.lifelink.dating.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateDatingRecordRequest {

    @NotNull(message = "Dating date is required")
    private LocalDate datingDate;

    @Valid
    @NotEmpty(message = "At least one activity is required")
    @Size(max = 20, message = "Activities must contain at most 20 items")
    private List<@NotBlank(message = "Activity cannot be blank") @Size(max = 100, message = "Activity length must be at most 100") String> activities;

    @Size(max = 200, message = "Location length must be at most 200")
    private String location;

    @Size(max = 2000, message = "Note length must be at most 2000")
    private String note;
}
