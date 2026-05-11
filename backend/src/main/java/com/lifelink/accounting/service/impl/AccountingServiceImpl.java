package com.lifelink.accounting.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifelink.accounting.dto.AccountBookResponse;
import com.lifelink.accounting.dto.CategoryStatisticResponse;
import com.lifelink.accounting.dto.CreateAccountBookRequest;
import com.lifelink.accounting.dto.CreateTransactionRequest;
import com.lifelink.accounting.dto.MonthlyFinanceSummaryResponse;
import com.lifelink.accounting.dto.TransactionCategoryResponse;
import com.lifelink.accounting.dto.TransactionResponse;
import com.lifelink.accounting.dto.UpdateTransactionRequest;
import com.lifelink.accounting.entity.AccountBook;
import com.lifelink.accounting.entity.Transaction;
import com.lifelink.accounting.entity.TransactionCategory;
import com.lifelink.accounting.mapper.AccountBookMapper;
import com.lifelink.accounting.mapper.TransactionCategoryMapper;
import com.lifelink.accounting.mapper.TransactionMapper;
import com.lifelink.accounting.service.AccountingService;
import com.lifelink.common.BusinessException;
import com.lifelink.relationship.entity.Relationship;
import com.lifelink.relationship.entity.RelationshipMember;
import com.lifelink.relationship.mapper.RelationshipMapper;
import com.lifelink.relationship.mapper.RelationshipMemberMapper;
import com.lifelink.user.entity.User;
import com.lifelink.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AccountingServiceImpl implements AccountingService {

    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String DELETED_STATUS = "DELETED";
    private static final String PERSONAL_TYPE = "PERSONAL";
    private static final String RELATIONSHIP_TYPE = "RELATIONSHIP";
    private static final String INCOME_TYPE = "INCOME";
    private static final String EXPENSE_TYPE = "EXPENSE";

    private final AccountBookMapper accountBookMapper;
    private final TransactionCategoryMapper categoryMapper;
    private final TransactionMapper transactionMapper;
    private final RelationshipMapper relationshipMapper;
    private final RelationshipMemberMapper relationshipMemberMapper;
    private final UserMapper userMapper;

    @Override
    public List<AccountBookResponse> listAccountBooks(Long userId) {
        Set<Long> relationshipIds = listCurrentUserRelationshipIds(userId);
        LambdaQueryWrapper<AccountBook> wrapper = new LambdaQueryWrapper<AccountBook>()
                .eq(AccountBook::getStatus, ACTIVE_STATUS)
                .and(item -> item
                        .eq(AccountBook::getOwnerId, userId)
                        .or(relationshipIds.size() > 0, q -> q.in(AccountBook::getRelationshipId, relationshipIds)));
        List<AccountBook> books = accountBookMapper.selectList(wrapper);
        List<AccountBookResponse> responses = new ArrayList<AccountBookResponse>();
        for (AccountBook book : books) {
            if (canAccessBook(book, userId)) {
                responses.add(toBookResponse(book));
            }
        }
        return responses;
    }

    @Override
    @Transactional
    public AccountBookResponse createAccountBook(CreateAccountBookRequest request, Long userId) {
        String type = request.getType().trim().toUpperCase();
        if (!PERSONAL_TYPE.equals(type) && !RELATIONSHIP_TYPE.equals(type)) {
            throw new BusinessException(400, "Invalid account book type");
        }
        if (PERSONAL_TYPE.equals(type) && request.getRelationshipId() != null) {
            throw new BusinessException(400, "Personal account book cannot have relationshipId");
        }
        if (RELATIONSHIP_TYPE.equals(type)) {
            if (request.getRelationshipId() == null) {
                throw new BusinessException(400, "Relationship account book requires relationshipId");
            }
            requireRelationshipMember(request.getRelationshipId(), userId);
        }

        LocalDateTime now = LocalDateTime.now();
        AccountBook book = new AccountBook();
        book.setRelationshipId(request.getRelationshipId());
        book.setOwnerId(userId);
        book.setName(request.getName().trim());
        book.setType(type);
        book.setStatus(ACTIVE_STATUS);
        book.setCreatedAt(now);
        book.setUpdatedAt(now);
        accountBookMapper.insert(book);
        return toBookResponse(book);
    }

    @Override
    public List<TransactionCategoryResponse> listCategories(String type) {
        LambdaQueryWrapper<TransactionCategory> wrapper = new LambdaQueryWrapper<TransactionCategory>()
                .eq(TransactionCategory::getStatus, ACTIVE_STATUS)
                .orderByAsc(TransactionCategory::getSortOrder)
                .orderByAsc(TransactionCategory::getId);
        if (StringUtils.hasText(type)) {
            wrapper.eq(TransactionCategory::getType, type.trim().toUpperCase());
        }
        List<TransactionCategory> categories = categoryMapper.selectList(wrapper);
        List<TransactionCategoryResponse> responses = new ArrayList<TransactionCategoryResponse>();
        for (TransactionCategory category : categories) {
            responses.add(new TransactionCategoryResponse(category.getId(), category.getName(), category.getType(), category.getIcon(), category.getSortOrder()));
        }
        return responses;
    }

    @Override
    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request, Long userId) {
        AccountBook book = requireAccessibleBook(request.getAccountBookId(), userId);
        validateTransactionType(request.getType());
        validateCategory(request.getCategoryId(), request.getType());

        LocalDateTime now = LocalDateTime.now();
        Transaction transaction = new Transaction();
        transaction.setAccountBookId(book.getId());
        transaction.setUserId(userId);
        transaction.setType(request.getType().trim().toUpperCase());
        transaction.setAmount(request.getAmount());
        transaction.setCategoryId(request.getCategoryId());
        transaction.setTitle(request.getTitle().trim());
        transaction.setNote(request.getNote());
        transaction.setTransactionTime(request.getTransactionTime());
        transaction.setStatus(ACTIVE_STATUS);
        transaction.setCreatedAt(now);
        transaction.setUpdatedAt(now);
        transactionMapper.insert(transaction);
        return toTransactionResponse(transaction, book);
    }

    @Override
    public List<TransactionResponse> listTransactions(Long accountBookId, Long relationshipId, String type, String startDate, String endDate, Integer page, Integer size, Long userId) {
        List<Long> accessibleBookIds = resolveAccessibleBookIds(accountBookId, relationshipId, userId);
        if (accessibleBookIds.isEmpty()) {
            return new ArrayList<TransactionResponse>();
        }

        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<Transaction>()
                .in(Transaction::getAccountBookId, accessibleBookIds)
                .eq(Transaction::getStatus, ACTIVE_STATUS);
        if (StringUtils.hasText(type)) {
            wrapper.eq(Transaction::getType, type.trim().toUpperCase());
        }
        if (StringUtils.hasText(startDate)) {
            wrapper.ge(Transaction::getTransactionTime, parseDateStart(startDate));
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.lt(Transaction::getTransactionTime, parseDateEndExclusive(endDate));
        }
        wrapper.orderByDesc(Transaction::getTransactionTime).orderByDesc(Transaction::getCreatedAt);

        long current = page == null || page < 1 ? 1L : page.longValue();
        long pageSize = size == null || size < 1 ? 20L : Math.min(size.longValue(), 100L);
        Page<Transaction> result = transactionMapper.selectPage(new Page<Transaction>(current, pageSize), wrapper);
        List<TransactionResponse> responses = new ArrayList<TransactionResponse>();
        for (Transaction transaction : result.getRecords()) {
            responses.add(toTransactionResponse(transaction, accountBookMapper.selectById(transaction.getAccountBookId())));
        }
        return responses;
    }

    @Override
    @Transactional
    public TransactionResponse updateTransaction(Long transactionId, UpdateTransactionRequest request, Long userId) {
        Transaction transaction = requireActiveTransaction(transactionId);
        AccountBook book = requireEditableBook(transaction.getAccountBookId(), userId);
        validateTransactionType(request.getType());
        validateCategory(request.getCategoryId(), request.getType());

        transaction.setType(request.getType().trim().toUpperCase());
        transaction.setAmount(request.getAmount());
        transaction.setCategoryId(request.getCategoryId());
        transaction.setTitle(request.getTitle().trim());
        transaction.setNote(request.getNote());
        transaction.setTransactionTime(request.getTransactionTime());
        transaction.setUpdatedAt(LocalDateTime.now());
        transactionMapper.updateById(transaction);
        return toTransactionResponse(transaction, book);
    }

    @Override
    @Transactional
    public void deleteTransaction(Long transactionId, Long userId) {
        Transaction transaction = requireActiveTransaction(transactionId);
        requireEditableBook(transaction.getAccountBookId(), userId);
        transaction.setStatus(DELETED_STATUS);
        transaction.setUpdatedAt(LocalDateTime.now());
        transactionMapper.updateById(transaction);
    }

    @Override
    public MonthlyFinanceSummaryResponse getMonthlySummary(Long accountBookId, Long relationshipId, String month, Long userId) {
        List<Transaction> transactions = listStatisticTransactions(accountBookId, relationshipId, month, userId);
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;
        for (Transaction transaction : transactions) {
            if (INCOME_TYPE.equals(transaction.getType())) {
                income = income.add(transaction.getAmount());
            } else if (EXPENSE_TYPE.equals(transaction.getType())) {
                expense = expense.add(transaction.getAmount());
            }
        }
        return new MonthlyFinanceSummaryResponse(income, expense, income.subtract(expense));
    }

    @Override
    public List<CategoryStatisticResponse> getCategoryStatistics(Long accountBookId, Long relationshipId, String type, String month, Long userId) {
        String targetType = StringUtils.hasText(type) ? type.trim().toUpperCase() : EXPENSE_TYPE;
        validateTransactionType(targetType);
        List<Transaction> transactions = listStatisticTransactions(accountBookId, relationshipId, month, userId);
        Map<Long, BigDecimal> amountMap = new HashMap<Long, BigDecimal>();
        BigDecimal total = BigDecimal.ZERO;
        for (Transaction transaction : transactions) {
            if (targetType.equals(transaction.getType())) {
                Long categoryId = transaction.getCategoryId() == null ? 0L : transaction.getCategoryId();
                amountMap.put(categoryId, amountMap.getOrDefault(categoryId, BigDecimal.ZERO).add(transaction.getAmount()));
                total = total.add(transaction.getAmount());
            }
        }

        List<CategoryStatisticResponse> responses = new ArrayList<CategoryStatisticResponse>();
        for (Map.Entry<Long, BigDecimal> entry : amountMap.entrySet()) {
            String categoryName = "Other";
            if (entry.getKey() != 0L) {
                TransactionCategory category = categoryMapper.selectById(entry.getKey());
                if (category != null) {
                    categoryName = category.getName();
                }
            }
            BigDecimal percentage = total.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : entry.getValue().multiply(new BigDecimal("100")).divide(total, 2, RoundingMode.HALF_UP);
            responses.add(new CategoryStatisticResponse(categoryName, entry.getValue(), percentage));
        }
        return responses;
    }

    private List<Transaction> listStatisticTransactions(Long accountBookId, Long relationshipId, String month, Long userId) {
        List<Long> bookIds = resolveAccessibleBookIds(accountBookId, relationshipId, userId);
        if (bookIds.isEmpty()) {
            return new ArrayList<Transaction>();
        }
        YearMonth yearMonth = parseMonth(month);
        return transactionMapper.selectList(new LambdaQueryWrapper<Transaction>()
                .in(Transaction::getAccountBookId, bookIds)
                .eq(Transaction::getStatus, ACTIVE_STATUS)
                .ge(Transaction::getTransactionTime, yearMonth.atDay(1).atStartOfDay())
                .lt(Transaction::getTransactionTime, yearMonth.plusMonths(1).atDay(1).atStartOfDay()));
    }

    private List<Long> resolveAccessibleBookIds(Long accountBookId, Long relationshipId, Long userId) {
        List<AccountBook> candidates;
        if (accountBookId != null) {
            AccountBook book = requireAccessibleBook(accountBookId, userId);
            candidates = new ArrayList<AccountBook>();
            candidates.add(book);
        } else if (relationshipId != null) {
            requireRelationshipMember(relationshipId, userId);
            candidates = accountBookMapper.selectList(new LambdaQueryWrapper<AccountBook>()
                    .eq(AccountBook::getStatus, ACTIVE_STATUS)
                    .eq(AccountBook::getRelationshipId, relationshipId));
        } else {
            candidates = accountBookMapper.selectList(new LambdaQueryWrapper<AccountBook>().eq(AccountBook::getStatus, ACTIVE_STATUS));
        }

        List<Long> bookIds = new ArrayList<Long>();
        for (AccountBook book : candidates) {
            if (canAccessBook(book, userId)) {
                bookIds.add(book.getId());
            }
        }
        return bookIds;
    }

    private AccountBook requireAccessibleBook(Long bookId, Long userId) {
        AccountBook book = accountBookMapper.selectById(bookId);
        if (book == null || !ACTIVE_STATUS.equals(book.getStatus()) || !canAccessBook(book, userId)) {
            throw new BusinessException(404, "Account book not found");
        }
        return book;
    }

    private AccountBook requireEditableBook(Long bookId, Long userId) {
        AccountBook book = requireAccessibleBook(bookId, userId);
        if (PERSONAL_TYPE.equals(book.getType()) && !userId.equals(book.getOwnerId())) {
            throw new BusinessException(403, "No permission to edit this transaction");
        }
        return book;
    }

    private boolean canAccessBook(AccountBook book, Long userId) {
        if (PERSONAL_TYPE.equals(book.getType())) {
            return userId.equals(book.getOwnerId());
        }
        return RELATIONSHIP_TYPE.equals(book.getType())
                && book.getRelationshipId() != null
                && isRelationshipMember(book.getRelationshipId(), userId);
    }

    private Transaction requireActiveTransaction(Long transactionId) {
        Transaction transaction = transactionMapper.selectById(transactionId);
        if (transaction == null || !ACTIVE_STATUS.equals(transaction.getStatus())) {
            throw new BusinessException(404, "Transaction not found");
        }
        return transaction;
    }

    private void validateTransactionType(String type) {
        String normalized = StringUtils.hasText(type) ? type.trim().toUpperCase() : "";
        if (!INCOME_TYPE.equals(normalized) && !EXPENSE_TYPE.equals(normalized)) {
            throw new BusinessException(400, "Invalid transaction type");
        }
    }

    private void validateCategory(Long categoryId, String type) {
        if (categoryId == null) {
            return;
        }
        TransactionCategory category = categoryMapper.selectById(categoryId);
        String normalizedType = type.trim().toUpperCase();
        if (category == null || !ACTIVE_STATUS.equals(category.getStatus()) || !normalizedType.equals(category.getType())) {
            throw new BusinessException(400, "Invalid transaction category");
        }
    }

    private void requireRelationshipMember(Long relationshipId, Long userId) {
        Relationship relationship = relationshipMapper.selectById(relationshipId);
        if (relationship == null || !ACTIVE_STATUS.equals(relationship.getStatus())) {
            throw new BusinessException(404, "Relationship not found");
        }
        if (!isRelationshipMember(relationshipId, userId)) {
            throw new BusinessException(403, "You are not a member of this relationship");
        }
    }

    private boolean isRelationshipMember(Long relationshipId, Long userId) {
        RelationshipMember member = relationshipMemberMapper.selectOne(new LambdaQueryWrapper<RelationshipMember>()
                .eq(RelationshipMember::getRelationshipId, relationshipId)
                .eq(RelationshipMember::getUserId, userId)
                .last("LIMIT 1"));
        return member != null;
    }

    private Set<Long> listCurrentUserRelationshipIds(Long userId) {
        List<RelationshipMember> members = relationshipMemberMapper.selectList(new LambdaQueryWrapper<RelationshipMember>()
                .eq(RelationshipMember::getUserId, userId));
        Set<Long> ids = new HashSet<Long>();
        for (RelationshipMember member : members) {
            ids.add(member.getRelationshipId());
        }
        return ids;
    }

    private YearMonth parseMonth(String month) {
        if (StringUtils.hasText(month)) {
            return YearMonth.parse(month);
        }
        return YearMonth.now();
    }

    private LocalDateTime parseDateStart(String date) {
        return LocalDate.parse(date).atStartOfDay();
    }

    private LocalDateTime parseDateEndExclusive(String date) {
        return LocalDate.parse(date).plusDays(1).atStartOfDay();
    }

    private AccountBookResponse toBookResponse(AccountBook book) {
        Relationship relationship = book.getRelationshipId() == null ? null : relationshipMapper.selectById(book.getRelationshipId());
        return new AccountBookResponse(
                book.getId(),
                book.getRelationshipId(),
                relationship == null ? null : relationship.getName(),
                book.getOwnerId(),
                book.getName(),
                book.getType(),
                book.getStatus(),
                book.getCreatedAt()
        );
    }

    private TransactionResponse toTransactionResponse(Transaction transaction, AccountBook book) {
        User user = userMapper.selectById(transaction.getUserId());
        TransactionCategory category = transaction.getCategoryId() == null ? null : categoryMapper.selectById(transaction.getCategoryId());
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAccountBookId(),
                book == null ? null : book.getName(),
                book == null ? null : book.getRelationshipId(),
                transaction.getUserId(),
                user == null ? null : user.getUsername(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getCategoryId(),
                category == null ? null : category.getName(),
                transaction.getTitle(),
                transaction.getNote(),
                transaction.getTransactionTime(),
                transaction.getStatus(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }
}
