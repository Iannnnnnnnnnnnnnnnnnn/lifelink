package com.lifelink.todo.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateSpaceTodoRequest {

    @Size(min = 1, max = 100, message = "Title length must be between 1 and 100")
    private String title;

    private String content;

    @Pattern(regexp = "LOW|NORMAL|HIGH", message = "Priority is invalid")
    private String priority;

    private LocalDateTime dueTime;
}
