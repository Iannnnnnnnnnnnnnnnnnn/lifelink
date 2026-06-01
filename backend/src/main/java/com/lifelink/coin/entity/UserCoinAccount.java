package com.lifelink.coin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_coin_accounts")
public class UserCoinAccount {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer balance;

    private Integer totalEarned;

    private Integer totalSpent;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
