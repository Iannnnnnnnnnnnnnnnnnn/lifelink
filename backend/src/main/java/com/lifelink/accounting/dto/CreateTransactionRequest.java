package com.lifelink.accounting.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateTransactionRequest {

    @NotNull(message = "Account book is required")
    private Long accountBookId;

    @NotBlank(message = "Type is required")
    private String type;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private Long categoryId;

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title length must be at most 100")
    private String title;

    private String note;

    @NotNull(message = "Transaction time is required")
    private LocalDateTime transactionTime;
}
