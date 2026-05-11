package com.lifelink.accounting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private Long id;

    private Long accountBookId;

    private String accountBookName;

    private Long relationshipId;

    private Long userId;

    private String username;

    private String type;

    private BigDecimal amount;

    private Long categoryId;

    private String categoryName;

    private String title;

    private String note;

    private LocalDateTime transactionTime;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
