package com.lifelink.daily.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentDailyPostRequest {

    @NotBlank(message = "Comment content is required")
    @Size(max = 1000, message = "Comment length must be at most 1000")
    private String content;
}
