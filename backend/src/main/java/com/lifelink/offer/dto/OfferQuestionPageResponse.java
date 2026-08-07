package com.lifelink.offer.dto;

import com.lifelink.offer.entity.OfferQuestion;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class OfferQuestionPageResponse {
    private List<OfferQuestion> records;
    private long total;
    private long page;
    private long size;
}
