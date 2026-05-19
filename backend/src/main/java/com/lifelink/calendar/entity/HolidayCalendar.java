package com.lifelink.calendar.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("holiday_calendar")
public class HolidayCalendar {

    @TableId(type = IdType.AUTO)
    private Long id;

    private LocalDate date;

    private String nameZh;

    private String nameEn;

    private String type;

    private String lunarDate;

    @TableField("is_holiday")
    private Boolean holiday;

    @TableField("is_workday")
    private Boolean workday;

    private String descriptionZh;

    private String descriptionEn;

    private LocalDateTime createdAt;
}
