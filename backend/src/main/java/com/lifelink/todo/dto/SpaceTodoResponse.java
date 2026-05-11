package com.lifelink.todo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpaceTodoResponse {

    private Long id;
    private Long relationshipId;
    private String title;
    private String content;
    private String priority;
    private String status;
    private LocalDateTime dueTime;
    private Long createdBy;
    private String createdByUsername;
    private Long updatedBy;
    private Long completedBy;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
