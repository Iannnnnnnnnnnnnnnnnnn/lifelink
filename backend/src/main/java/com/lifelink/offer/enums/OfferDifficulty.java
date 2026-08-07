package com.lifelink.offer.enums;

import com.baomidou.mybatisplus.annotation.IEnum;

public enum OfferDifficulty implements IEnum<String> {
    EASY,
    MEDIUM,
    HARD;

    @Override
    public String getValue() {
        return name();
    }
}
