package com.lifelink.offer.dto;

import lombok.Data;

import java.util.List;

@Data
public class OfferImportResultResponse {
    private int total;
    private int valid;
    private int invalid;
    private int duplicate;
    private int imported;
    private List<OfferImportQuestionResponse> questions;
}
