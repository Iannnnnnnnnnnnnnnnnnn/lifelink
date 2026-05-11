package com.lifelink.accounting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCategoryResponse {

    private Long id;

    private String name;

    private String type;

    private String icon;

    private Integer sortOrder;
}
