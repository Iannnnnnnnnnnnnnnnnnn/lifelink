package com.lifelink.todo.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateSpaceTodoRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title length must be at most 100")
    private String title;

    private String content;

    @Pattern(regexp = "LOW|NORMAL|HIGH", message = "Priority is invalid")
    private String priority;

    private LocalDateTime dueTime;
}
