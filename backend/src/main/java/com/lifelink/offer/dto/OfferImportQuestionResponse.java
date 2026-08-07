package com.lifelink.offer.dto;

import com.lifelink.offer.enums.OfferDifficulty;
import com.lifelink.offer.enums.OfferQuestionType;
import lombok.Data;

import java.util.List;

@Data
public class OfferImportQuestionResponse {
    private int index;
    private OfferQuestionType type;
    private String bank;
    private String category;
    private OfferDifficulty difficulty;
    private String title;
    private String source;
    private String status;
    private List<String> errors;
}
