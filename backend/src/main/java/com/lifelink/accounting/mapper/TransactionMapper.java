package com.lifelink.accounting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lifelink.accounting.entity.Transaction;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TransactionMapper extends BaseMapper<Transaction> {
}
