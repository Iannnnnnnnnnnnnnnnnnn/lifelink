package com.lifelink.accounting.controller;

import com.lifelink.accounting.dto.CategoryStatisticResponse;
import com.lifelink.accounting.dto.MonthlyFinanceSummaryResponse;
import com.lifelink.accounting.service.AccountingService;
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
@RequestMapping("/api/statistics/finance")
@RequiredArgsConstructor
public class FinanceStatisticsController {

    private final AccountingService accountingService;

    @GetMapping("/monthly")
    public Result<MonthlyFinanceSummaryResponse> getMonthlySummary(@RequestParam(required = false) Long accountBookId,
                                                                   @RequestParam(required = false) Long relationshipId,
                                                                   @RequestParam(required = false) String month,
                                                                   @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(accountingService.getMonthlySummary(accountBookId, relationshipId, month, loginUser.getId()));
    }

    @GetMapping("/category")
    public Result<List<CategoryStatisticResponse>> getCategoryStatistics(@RequestParam(required = false) Long accountBookId,
                                                                         @RequestParam(required = false) Long relationshipId,
                                                                         @RequestParam(required = false) String type,
                                                                         @RequestParam(required = false) String month,
                                                                         @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(accountingService.getCategoryStatistics(accountBookId, relationshipId, type, month, loginUser.getId()));
    }
}
