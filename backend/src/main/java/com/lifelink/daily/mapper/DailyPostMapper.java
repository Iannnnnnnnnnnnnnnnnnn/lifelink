package com.lifelink.daily.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lifelink.daily.entity.DailyPost;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DailyPostMapper extends BaseMapper<DailyPost> {
}
