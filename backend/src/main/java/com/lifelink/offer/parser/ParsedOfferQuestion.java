package com.lifelink.offer.parser;

import com.lifelink.offer.enums.OfferDifficulty;
import com.lifelink.offer.enums.OfferQuestionType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ParsedOfferQuestion {
    private int index;
    private OfferQuestionType type;
    private String bank;
    private String category;
    private OfferDifficulty difficulty;
    private String title;
    private String source;
    private String content;
    private String answer;
    private List<String> errors = new ArrayList<>();

    public boolean isValid() {
        return errors.isEmpty();
    }

    public void addError(String error) {
        errors.add(error);
    }
}
