package com.lifelink.offer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lifelink.offer.enums.OfferQuestionType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("offer_categories")
public class OfferCategory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long bankId;
    private String name;
    private OfferQuestionType type;
    private Integer sort;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
