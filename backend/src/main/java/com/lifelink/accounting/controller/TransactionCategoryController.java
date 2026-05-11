package com.lifelink.accounting.controller;

import com.lifelink.accounting.dto.TransactionCategoryResponse;
import com.lifelink.accounting.service.AccountingService;
import com.lifelink.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transaction-categories")
@RequiredArgsConstructor
public class TransactionCategoryController {

    private final AccountingService accountingService;

    @GetMapping
    public Result<List<TransactionCategoryResponse>> listCategories(@RequestParam(required = false) String type) {
        return Result.success(accountingService.listCategories(type));
    }
}
