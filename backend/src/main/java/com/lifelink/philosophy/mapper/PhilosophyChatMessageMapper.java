package com.lifelink.philosophy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lifelink.philosophy.entity.PhilosophyChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PhilosophyChatMessageMapper extends BaseMapper<PhilosophyChatMessage> {
}
