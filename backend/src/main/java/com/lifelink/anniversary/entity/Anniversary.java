package com.lifelink.anniversary.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("anniversaries")
public class Anniversary {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long relationshipId;

    private String title;

    private String description;

    private LocalDate anniversaryDate;

    private String repeatType;

    private Long backgroundFileId;

    private String backgroundUrl;

    private Long createdBy;

    private Long updatedBy;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
