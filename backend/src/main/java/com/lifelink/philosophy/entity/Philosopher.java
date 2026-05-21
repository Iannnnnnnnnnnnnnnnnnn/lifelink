package com.lifelink.philosophy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lifelink.common.typehandler.JsonbStringTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "philosophers", autoResultMap = true)
public class Philosopher {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    private String nameZh;

    private String nameEn;

    private String eraZh;

    private String eraEn;

    private String descriptionZh;

    private String descriptionEn;

    private String avatarUrl;

    @TableField(typeHandler = JsonbStringTypeHandler.class)
    private String tags;

    private Integer sortOrder;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
