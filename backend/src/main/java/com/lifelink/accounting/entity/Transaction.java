package com.lifelink.accounting.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("transactions")
public class Transaction {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long accountBookId;

    private Long userId;

    private String type;

    private BigDecimal amount;

    private Long categoryId;

    private String title;

    private String note;

    private LocalDateTime transactionTime;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
