package com.lifelink.offer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OfferQuestionBankRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String code;
    private Integer sort = 0;
}
