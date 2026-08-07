package com.lifelink.offer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lifelink.offer.enums.OfferDifficulty;
import com.lifelink.offer.enums.OfferQuestionType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("offer_questions")
public class OfferQuestion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private OfferQuestionType type;
    private Long bankId;
    private Long categoryId;
    private OfferDifficulty difficulty;
    private String content;
    private String answer;
    private String source;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
