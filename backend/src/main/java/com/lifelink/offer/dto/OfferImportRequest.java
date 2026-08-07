package com.lifelink.offer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OfferImportRequest {
    @NotBlank
    private String content;
}
