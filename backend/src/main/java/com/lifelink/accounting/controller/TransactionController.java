package com.lifelink.accounting.controller;

import com.lifelink.accounting.dto.CreateTransactionRequest;
import com.lifelink.accounting.dto.TransactionResponse;
import com.lifelink.accounting.dto.UpdateTransactionRequest;
import com.lifelink.accounting.service.AccountingService;
import com.lifelink.common.Result;
import com.lifelink.security.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final AccountingService accountingService;

    @PostMapping
    public Result<TransactionResponse> createTransaction(@Valid @RequestBody CreateTransactionRequest request,
                                                         @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(accountingService.createTransaction(request, loginUser.getId()));
    }

    @GetMapping
    public Result<List<TransactionResponse>> listTransactions(@RequestParam(required = false) Long accountBookId,
                                                              @RequestParam(required = false) Long relationshipId,
                                                              @RequestParam(required = false) String type,
                                                              @RequestParam(required = false) String startDate,
                                                              @RequestParam(required = false) String endDate,
                                                              @RequestParam(defaultValue = "1") Integer page,
                                                              @RequestParam(defaultValue = "20") Integer size,
                                                              @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(accountingService.listTransactions(accountBookId, relationshipId, type, startDate, endDate, page, size, loginUser.getId()));
    }

    @PutMapping("/{id}")
    public Result<TransactionResponse> updateTransaction(@PathVariable Long id,
                                                         @Valid @RequestBody UpdateTransactionRequest request,
                                                         @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(accountingService.updateTransaction(id, request, loginUser.getId()));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteTransaction(@PathVariable Long id,
                                          @AuthenticationPrincipal LoginUser loginUser) {
        accountingService.deleteTransaction(id, loginUser.getId());
        return Result.success();
    }
}
