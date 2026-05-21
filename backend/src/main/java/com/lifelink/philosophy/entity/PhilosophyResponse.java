package com.lifelink.philosophy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("philosophy_responses")
public class PhilosophyResponse {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;

    private String philosopherCode;

    private String philosopherName;

    private String viewpoint;

    private String questionBack;

    private String objection;

    private String summary;

    private String rawResponse;

    private String status;

    private LocalDateTime createdAt;
}
