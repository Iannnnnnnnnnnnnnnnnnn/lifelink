package com.lifelink.relationship.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("relationships")
public class Relationship {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String type;

    private String description;

    private Long ownerId;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
