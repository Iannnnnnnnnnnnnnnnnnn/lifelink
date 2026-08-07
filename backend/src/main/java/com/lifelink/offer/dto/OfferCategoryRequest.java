package com.lifelink.offer.dto;

import com.lifelink.offer.enums.OfferQuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OfferCategoryRequest {
    @NotNull
    private Long bankId;
    @NotBlank
    private String name;
    @NotNull
    private OfferQuestionType type;
    private Integer sort = 0;
}
