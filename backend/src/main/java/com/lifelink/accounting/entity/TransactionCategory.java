package com.lifelink.accounting.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("transaction_categories")
public class TransactionCategory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String type;

    private String icon;

    private Integer sortOrder;

    private String status;
}
