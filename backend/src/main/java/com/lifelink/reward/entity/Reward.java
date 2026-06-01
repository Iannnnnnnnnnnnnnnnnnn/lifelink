package com.lifelink.reward.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("rewards")
public class Reward {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    private String coverObjectKey;

    private String coverUrl;

    private Integer coinCost;

    private Integer stock;

    private Integer redeemedCount;

    private String status;

    private Integer sortOrder;

    private Long createdBy;

    private Long updatedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
