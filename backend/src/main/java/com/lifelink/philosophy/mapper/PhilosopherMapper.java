package com.lifelink.philosophy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lifelink.philosophy.entity.Philosopher;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PhilosopherMapper extends BaseMapper<Philosopher> {
}
