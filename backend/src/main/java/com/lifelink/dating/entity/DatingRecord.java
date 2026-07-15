package com.lifelink.dating.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lifelink.common.typehandler.JsonbStringTypeHandler;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName(value = "dating_records", autoResultMap = true)
public class DatingRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long relationshipId;

    private LocalDate datingDate;

    @TableField(typeHandler = JsonbStringTypeHandler.class)
    private String activities;

    private String location;

    private String note;

    private Long createdBy;

    private Long updatedBy;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
