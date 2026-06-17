package com.lifelink.coin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("coin_ledger")
public class CoinLedger {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer changeAmount;

    private Integer balanceAfter;

    private String type;

    private String sourceType;

    private Long sourceId;

    private String title;

    private String description;

    private LocalDateTime createdAt;
}
