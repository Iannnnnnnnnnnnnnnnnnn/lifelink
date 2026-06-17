package com.lifelink.coin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifelink.coin.dto.CoinAccountResponse;
import com.lifelink.coin.entity.CoinLedger;
import com.lifelink.coin.entity.UserCoinAccount;
import com.lifelink.coin.mapper.CoinLedgerMapper;
import com.lifelink.coin.mapper.UserCoinAccountMapper;
import com.lifelink.coin.service.CoinAccountService;
import com.lifelink.common.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CoinAccountServiceImpl implements CoinAccountService {

    private static final String EARN = "EARN";
    private static final String SPEND = "SPEND";

    private final UserCoinAccountMapper accountMapper;
    private final CoinLedgerMapper ledgerMapper;

    @Override
    @Transactional
    public CoinAccountResponse getAccount(Long userId) {
        UserCoinAccount account = getOrCreateAccount(userId);
        return new CoinAccountResponse(account.getBalance(), account.getTotalEarned(), account.getTotalSpent());
    }

    @Override
    @Transactional
    public UserCoinAccount getOrCreateAccount(Long userId) {
        UserCoinAccount existing = findByUserId(userId);
        if (existing != null) {
            return existing;
        }
        LocalDateTime now = LocalDateTime.now();
        UserCoinAccount account = new UserCoinAccount();
        account.setUserId(userId);
        account.setBalance(0);
        account.setTotalEarned(0);
        account.setTotalSpent(0);
        account.setCreatedAt(now);
        account.setUpdatedAt(now);
        try {
            accountMapper.insert(account);
            return account;
        } catch (DuplicateKeyException ex) {
            return findByUserId(userId);
        }
    }

    @Override
    @Transactional
    public Integer earn(Long userId, Integer amount, String sourceType, Long sourceId, String title, String description) {
        int safeAmount = safePositiveAmount(amount);
        getOrCreateAccount(userId);
        accountMapper.addEarned(userId, safeAmount);
        UserCoinAccount account = findByUserId(userId);
        insertLedger(userId, safeAmount, account.getBalance(), EARN, sourceType, sourceId, title, description);
        return account.getBalance();
    }

    @Override
    @Transactional
    public Integer spend(Long userId, Integer amount, String sourceType, Long sourceId, String title, String description) {
        int safeAmount = safePositiveAmount(amount);
        getOrCreateAccount(userId);
        int updated = accountMapper.spendIfEnough(userId, safeAmount);
        if (updated == 0) {
            throw new BusinessException(400, "专注币不足");
        }
        UserCoinAccount account = findByUserId(userId);
        insertLedger(userId, -safeAmount, account.getBalance(), SPEND, sourceType, sourceId, title, description);
        return account.getBalance();
    }

    private UserCoinAccount findByUserId(Long userId) {
        return accountMapper.selectOne(new LambdaQueryWrapper<UserCoinAccount>()
                .eq(UserCoinAccount::getUserId, userId)
                .last("LIMIT 1"));
    }

    private void insertLedger(Long userId, Integer changeAmount, Integer balanceAfter, String type,
                              String sourceType, Long sourceId, String title, String description) {
        CoinLedger ledger = new CoinLedger();
        ledger.setUserId(userId);
        ledger.setChangeAmount(changeAmount);
        ledger.setBalanceAfter(balanceAfter);
        ledger.setType(type);
        ledger.setSourceType(sourceType);
        ledger.setSourceId(sourceId);
        ledger.setTitle(title);
        ledger.setDescription(description);
        ledger.setCreatedAt(LocalDateTime.now());
        ledgerMapper.insert(ledger);
    }

    private int safePositiveAmount(Integer amount) {
        if (amount == null || amount <= 0) {
            throw new BusinessException(400, "Coin amount must be greater than 0");
        }
        return amount;
    }
}
