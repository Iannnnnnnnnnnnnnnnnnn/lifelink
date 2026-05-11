package com.lifelink.todo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("space_todos")
public class SpaceTodo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long relationshipId;

    private String title;

    private String content;

    private String priority;

    private String status;

    private LocalDateTime dueTime;

    private Long createdBy;

    private Long updatedBy;

    private Long completedBy;

    private LocalDateTime completedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
