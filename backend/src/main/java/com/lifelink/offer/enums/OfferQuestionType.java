package com.lifelink.offer.enums;

import com.baomidou.mybatisplus.annotation.IEnum;

public enum OfferQuestionType implements IEnum<String> {
    THEORY,
    ALGORITHM;

    @Override
    public String getValue() {
        return name();
    }
}
