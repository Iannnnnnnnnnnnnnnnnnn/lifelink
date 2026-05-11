package com.lifelink.accounting.service;

import com.lifelink.accounting.dto.AccountBookResponse;
import com.lifelink.accounting.dto.CategoryStatisticResponse;
import com.lifelink.accounting.dto.CreateAccountBookRequest;
import com.lifelink.accounting.dto.CreateTransactionRequest;
import com.lifelink.accounting.dto.MonthlyFinanceSummaryResponse;
import com.lifelink.accounting.dto.TransactionCategoryResponse;
import com.lifelink.accounting.dto.TransactionResponse;
import com.lifelink.accounting.dto.UpdateTransactionRequest;

import java.util.List;

public interface AccountingService {

    List<AccountBookResponse> listAccountBooks(Long userId);

    AccountBookResponse createAccountBook(CreateAccountBookRequest request, Long userId);

    List<TransactionCategoryResponse> listCategories(String type);

    TransactionResponse createTransaction(CreateTransactionRequest request, Long userId);

    List<TransactionResponse> listTransactions(Long accountBookId, Long relationshipId, String type, String startDate, String endDate, Integer page, Integer size, Long userId);

    TransactionResponse updateTransaction(Long transactionId, UpdateTransactionRequest request, Long userId);

    void deleteTransaction(Long transactionId, Long userId);

    MonthlyFinanceSummaryResponse getMonthlySummary(Long accountBookId, Long relationshipId, String month, Long userId);

    List<CategoryStatisticResponse> getCategoryStatistics(Long accountBookId, Long relationshipId, String type, String month, Long userId);
}
