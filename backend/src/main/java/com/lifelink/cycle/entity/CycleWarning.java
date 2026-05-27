package com.lifelink.cycle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("cycle_warnings")
public class CycleWarning {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long loverSpaceId;

    private String warningType;

    private LocalDate warningDate;

    private String severity;

    private String title;

    private String message;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
