package com.lifelink.cycle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("cycle_care_profiles")
public class CycleCareProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long defaultLoverSpaceId;

    private Integer cycleLength;

    private Integer periodLength;

    private LocalDate lastPeriodStartDate;

    private Boolean reminderEnabled;

    private Boolean dailyAdviceEnabled;

    private String shareLevel;

    private String timezone;

    private Boolean privacyNoteVisibleToPartner;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
