package com.lifelink.accounting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryStatisticResponse {

    private String categoryName;

    private BigDecimal amount;

    private BigDecimal percentage;
}
