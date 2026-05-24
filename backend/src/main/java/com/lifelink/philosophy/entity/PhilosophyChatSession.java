package com.lifelink.philosophy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("philosophy_chat_sessions")
public class PhilosophyChatSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String philosopherCode;

    private String philosopherName;

    private String title;

    private String language;

    private String status;

    private LocalDateTime lastMessageAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
