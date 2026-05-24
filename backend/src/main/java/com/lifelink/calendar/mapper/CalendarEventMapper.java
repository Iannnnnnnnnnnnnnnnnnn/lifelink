package com.lifelink.calendar.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lifelink.calendar.entity.CalendarEvent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CalendarEventMapper extends BaseMapper<CalendarEvent> {
}
