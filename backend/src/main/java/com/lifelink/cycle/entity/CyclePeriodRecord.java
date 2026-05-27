package com.lifelink.cycle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("cycle_period_records")
public class CyclePeriodRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long loverSpaceId;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer cycleLengthSnapshot;

    private Integer periodLengthSnapshot;

    private String note;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
