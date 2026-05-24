package com.lifelink.philosophy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("philosophy_sessions")
public class PhilosophySession {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String question;

    private String language;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
