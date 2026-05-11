package com.lifelink.accounting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lifelink.accounting.entity.TransactionCategory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TransactionCategoryMapper extends BaseMapper<TransactionCategory> {
}
