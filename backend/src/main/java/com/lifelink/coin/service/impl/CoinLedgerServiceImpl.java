package com.lifelink.coin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifelink.coin.dto.CoinLedgerResponse;
import com.lifelink.coin.entity.CoinLedger;
import com.lifelink.coin.mapper.CoinLedgerMapper;
import com.lifelink.coin.service.CoinLedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CoinLedgerServiceImpl implements CoinLedgerService {

    private final CoinLedgerMapper ledgerMapper;

    @Override
    public List<CoinLedgerResponse> listMyLedger(String startDate, String endDate, Integer page, Integer pageSize, Long userId) {
        LambdaQueryWrapper<CoinLedger> wrapper = new LambdaQueryWrapper<CoinLedger>()
                .eq(CoinLedger::getUserId, userId)
                .orderByDesc(CoinLedger::getCreatedAt);
        if (StringUtils.hasText(startDate)) {
            wrapper.ge(CoinLedger::getCreatedAt, LocalDate.parse(startDate).atStartOfDay());
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.lt(CoinLedger::getCreatedAt, LocalDate.parse(endDate).plusDays(1).atStartOfDay());
        }
        long current = page == null || page < 1 ? 1L : page.longValue();
        long size = pageSize == null || pageSize < 1 ? 20L : Math.min(pageSize.longValue(), 100L);
        return ledgerMapper.selectPage(new Page<CoinLedger>(current, size), wrapper).getRecords()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private CoinLedgerResponse toResponse(CoinLedger ledger) {
        LocalDateTime createdAt = ledger.getCreatedAt();
        return new CoinLedgerResponse(
                ledger.getId(),
                ledger.getChangeAmount(),
                ledger.getBalanceAfter(),
                ledger.getType(),
                ledger.getSourceType(),
                ledger.getSourceId(),
                ledger.getTitle(),
                ledger.getDescription(),
                createdAt
        );
    }
}
