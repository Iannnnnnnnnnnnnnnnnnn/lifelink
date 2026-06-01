package com.lifelink.coin.controller;

import com.lifelink.coin.dto.CoinAccountResponse;
import com.lifelink.coin.dto.CoinLedgerResponse;
import com.lifelink.coin.service.CoinAccountService;
import com.lifelink.coin.service.CoinLedgerService;
import com.lifelink.common.Result;
import com.lifelink.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/coins")
@RequiredArgsConstructor
public class CoinController {

    private final CoinAccountService coinAccountService;
    private final CoinLedgerService coinLedgerService;

    @GetMapping("/me")
    public Result<CoinAccountResponse> me(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(coinAccountService.getAccount(loginUser.getId()));
    }

    @GetMapping("/ledger")
    public Result<List<CoinLedgerResponse>> ledger(@RequestParam(required = false) String startDate,
                                                   @RequestParam(required = false) String endDate,
                                                   @RequestParam(required = false) Integer page,
                                                   @RequestParam(required = false) Integer pageSize,
                                                   @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(coinLedgerService.listMyLedger(startDate, endDate, page, pageSize, loginUser.getId()));
    }
}
