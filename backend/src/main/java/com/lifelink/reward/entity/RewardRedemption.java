package com.lifelink.reward.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("reward_redemptions")
public class RewardRedemption {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long rewardId;

    private Integer coinCostSnapshot;

    private String rewardTitleSnapshot;

    private String rewardDescriptionSnapshot;

    private String rewardCoverUrlSnapshot;

    private String status;

    private String note;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
