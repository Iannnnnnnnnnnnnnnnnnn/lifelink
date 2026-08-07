package com.lifelink.offer.dto;

import com.lifelink.offer.enums.OfferDifficulty;
import com.lifelink.offer.enums.OfferQuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OfferQuestionRequest {
    @NotBlank
    private String title;
    @NotNull
    private OfferQuestionType type;
    @NotNull
    private Long bankId;
    @NotNull
    private Long categoryId;
    @NotNull
    private OfferDifficulty difficulty;
    @NotBlank
    private String content;
    @NotBlank
    private String answer;
    private String source;
    private String remark;
}
