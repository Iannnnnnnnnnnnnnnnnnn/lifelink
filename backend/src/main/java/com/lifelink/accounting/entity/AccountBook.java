package com.lifelink.accounting.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("account_books")
public class AccountBook {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long relationshipId;

    private Long ownerId;

    private String name;

    private String type;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
