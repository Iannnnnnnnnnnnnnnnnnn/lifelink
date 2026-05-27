package com.lifelink.cycle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lifelink.common.typehandler.JsonbStringTypeHandler;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName(value = "cycle_daily_logs", autoResultMap = true)
public class CycleDailyLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long loverSpaceId;

    private LocalDate logDate;

    private String flowLevel;

    private Integer painLevel;

    private String mood;

    @TableField(typeHandler = JsonbStringTypeHandler.class)
    private String symptoms;

    private String temperatureFeeling;

    private String appetite;

    private String note;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
