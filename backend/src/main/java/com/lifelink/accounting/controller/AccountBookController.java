package com.lifelink.accounting.controller;

import com.lifelink.accounting.dto.AccountBookResponse;
import com.lifelink.accounting.dto.CreateAccountBookRequest;
import com.lifelink.accounting.service.AccountingService;
import com.lifelink.common.Result;
import com.lifelink.security.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/account-books")
@RequiredArgsConstructor
public class AccountBookController {

    private final AccountingService accountingService;

    @GetMapping
    public Result<List<AccountBookResponse>> listAccountBooks(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(accountingService.listAccountBooks(loginUser.getId()));
    }

    @PostMapping
    public Result<AccountBookResponse> createAccountBook(@Valid @RequestBody CreateAccountBookRequest request,
                                                         @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(accountingService.createAccountBook(request, loginUser.getId()));
    }
}
