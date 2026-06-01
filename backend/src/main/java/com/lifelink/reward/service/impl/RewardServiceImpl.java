package com.lifelink.reward.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifelink.coin.service.CoinAccountService;
import com.lifelink.common.BusinessException;
import com.lifelink.file.dto.FileUploadResponse;
import com.lifelink.file.service.FileService;
import com.lifelink.notification.service.NotificationService;
import com.lifelink.reward.dto.RewardAdminAccessResponse;
import com.lifelink.reward.dto.RewardCoverUploadResponse;
import com.lifelink.reward.dto.RewardRedeemResponse;
import com.lifelink.reward.dto.RewardRedemptionResponse;
import com.lifelink.reward.dto.RewardRequest;
import com.lifelink.reward.dto.RewardResponse;
import com.lifelink.reward.entity.Reward;
import com.lifelink.reward.entity.RewardRedemption;
import com.lifelink.reward.mapper.RewardMapper;
import com.lifelink.reward.mapper.RewardRedemptionMapper;
import com.lifelink.reward.service.RewardAdminAccessService;
import com.lifelink.reward.service.RewardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RewardServiceImpl implements RewardService {

    private static final String ACTIVE = "ACTIVE";
    private static final String INACTIVE = "INACTIVE";
    private static final String DRAFT = "DRAFT";
    private static final String SOLD_OUT = "SOLD_OUT";
    private static final String COMPLETED = "COMPLETED";
    private static final String REWARD_REDEMPTION = "REWARD_REDEMPTION";

    private final RewardMapper rewardMapper;
    private final RewardRedemptionMapper redemptionMapper;
    private final RewardAdminAccessService adminAccessService;
    private final CoinAccountService coinAccountService;
    private final FileService fileService;
    private final NotificationService notificationService;

    @Override
    public List<RewardResponse> listRewards(String status, String keyword, String sortBy, String sortDirection,
                                            Integer page, Integer pageSize, Long userId) {
        boolean admin = adminAccessService.isRewardAdmin(userId);
        LambdaQueryWrapper<Reward> wrapper = new LambdaQueryWrapper<Reward>();
        if (admin && StringUtils.hasText(status)) {
            wrapper.eq(Reward::getStatus, normalizeStatus(status));
        } else if (!admin) {
            wrapper.eq(Reward::getStatus, ACTIVE);
        }
        if (StringUtils.hasText(keyword)) {
            String like = keyword.trim();
            wrapper.and(item -> item.like(Reward::getTitle, like).or().like(Reward::getDescription, like));
        }
        applySorting(wrapper, sortBy, sortDirection);
        long current = page == null || page < 1 ? 1L : page.longValue();
        long size = pageSize == null || pageSize < 1 ? 20L : Math.min(pageSize.longValue(), 100L);
        return rewardMapper.selectPage(new Page<Reward>(current, size), wrapper).getRecords()
                .stream()
                .map(this::toRewardResponse)
                .toList();
    }

    @Override
    public RewardResponse getReward(Long id, Long userId) {
        Reward reward = requireReward(id);
        if (!ACTIVE.equals(reward.getStatus()) && !adminAccessService.isRewardAdmin(userId)) {
            throw new BusinessException(404, "Reward not found");
        }
        return toRewardResponse(reward);
    }

    @Override
    @Transactional
    public RewardRedeemResponse redeem(Long id, Long userId) {
        Reward reward = requireReward(id);
        if (!ACTIVE.equals(reward.getStatus())) {
            throw new BusinessException(400, "Reward is not available");
        }
        if (reward.getStock() != null && safeInt(reward.getRedeemedCount()) >= reward.getStock()) {
            throw new BusinessException(400, "Reward is sold out");
        }

        int stockUpdated = rewardMapper.incrementRedemptionIfAvailable(id);
        if (stockUpdated == 0) {
            throw new BusinessException(400, "Reward is sold out");
        }

        LocalDateTime now = LocalDateTime.now();
        RewardRedemption redemption = new RewardRedemption();
        redemption.setUserId(userId);
        redemption.setRewardId(reward.getId());
        redemption.setCoinCostSnapshot(reward.getCoinCost());
        redemption.setRewardTitleSnapshot(reward.getTitle());
        redemption.setRewardDescriptionSnapshot(reward.getDescription());
        redemption.setRewardCoverUrlSnapshot(reward.getCoverUrl());
        redemption.setStatus(COMPLETED);
        redemption.setCreatedAt(now);
        redemption.setUpdatedAt(now);
        redemptionMapper.insert(redemption);

        Integer balance = coinAccountService.spend(userId, reward.getCoinCost(), REWARD_REDEMPTION, redemption.getId(),
                "兑换奖励", "兑换「" + reward.getTitle() + "」");
        createRedeemedNotificationSafely(userId, reward, redemption);

        Reward refreshed = rewardMapper.selectById(id);
        if (refreshed != null && SOLD_OUT.equals(refreshed.getStatus())) {
            createSoldOutNotificationsSafely(refreshed);
        }
        return new RewardRedeemResponse(toRedemptionResponse(redemption), balance);
    }

    @Override
    public List<RewardRedemptionResponse> listMyRedemptions(Integer page, Integer pageSize, Long userId) {
        long current = page == null || page < 1 ? 1L : page.longValue();
        long size = pageSize == null || pageSize < 1 ? 20L : Math.min(pageSize.longValue(), 100L);
        return redemptionMapper.selectPage(new Page<RewardRedemption>(current, size),
                        new LambdaQueryWrapper<RewardRedemption>()
                                .eq(RewardRedemption::getUserId, userId)
                                .orderByDesc(RewardRedemption::getCreatedAt))
                .getRecords()
                .stream()
                .map(this::toRedemptionResponse)
                .toList();
    }

    @Override
    public RewardAdminAccessResponse getAdminAccess(Long userId) {
        return new RewardAdminAccessResponse(adminAccessService.isRewardAdmin(userId));
    }

    @Override
    @Transactional
    public RewardResponse createReward(RewardRequest request, Long userId) {
        adminAccessService.requireRewardAdmin(userId);
        LocalDateTime now = LocalDateTime.now();
        Reward reward = new Reward();
        applyRequest(reward, request);
        reward.setRedeemedCount(0);
        reward.setCreatedBy(userId);
        reward.setUpdatedBy(userId);
        reward.setCreatedAt(now);
        reward.setUpdatedAt(now);
        rewardMapper.insert(reward);
        return toRewardResponse(reward);
    }

    @Override
    @Transactional
    public RewardResponse updateReward(Long id, RewardRequest request, Long userId) {
        adminAccessService.requireRewardAdmin(userId);
        Reward reward = requireReward(id);
        applyRequest(reward, request);
        reward.setUpdatedBy(userId);
        reward.setUpdatedAt(LocalDateTime.now());
        if (SOLD_OUT.equals(reward.getStatus()) && (reward.getStock() == null || safeInt(reward.getRedeemedCount()) < reward.getStock())) {
            reward.setStatus(ACTIVE);
        }
        rewardMapper.updateById(reward);
        return toRewardResponse(reward);
    }

    @Override
    @Transactional
    public void deactivateReward(Long id, Long userId) {
        adminAccessService.requireRewardAdmin(userId);
        Reward reward = requireReward(id);
        reward.setStatus(INACTIVE);
        reward.setUpdatedBy(userId);
        reward.setUpdatedAt(LocalDateTime.now());
        rewardMapper.updateById(reward);
    }

    @Override
    @Transactional
    public RewardCoverUploadResponse uploadCover(MultipartFile file, Long userId) {
        adminAccessService.requireRewardAdmin(userId);
        FileUploadResponse upload = fileService.uploadRewardCoverImage(file, userId);
        return new RewardCoverUploadResponse(upload.getUrl(), upload.getObjectKey());
    }

    private Reward requireReward(Long id) {
        Reward reward = rewardMapper.selectById(id);
        if (reward == null) {
            throw new BusinessException(404, "Reward not found");
        }
        return reward;
    }

    private void applyRequest(Reward reward, RewardRequest request) {
        reward.setTitle(request.getTitle().trim());
        reward.setDescription(trimToNull(request.getDescription()));
        reward.setCoverObjectKey(trimToNull(request.getCoverObjectKey()));
        reward.setCoverUrl(trimToNull(request.getCoverUrl()));
        reward.setCoinCost(request.getCoinCost());
        reward.setStock(request.getStock());
        reward.setStatus(normalizeStatus(request.getStatus()));
        reward.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
    }

    private void applySorting(LambdaQueryWrapper<Reward> wrapper, String sortBy, String sortDirection) {
        boolean asc = "asc".equalsIgnoreCase(sortDirection);
        String field = StringUtils.hasText(sortBy) ? sortBy.trim() : "sortOrder";
        if ("coinCost".equals(field)) {
            wrapper.orderBy(true, asc, Reward::getCoinCost);
        } else {
            wrapper.orderBy(true, asc, Reward::getSortOrder);
        }
        wrapper.orderByDesc(Reward::getCreatedAt);
    }

    private String normalizeStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return DRAFT;
        }
        String value = status.trim().toUpperCase(Locale.ROOT);
        if (!List.of(DRAFT, ACTIVE, INACTIVE, SOLD_OUT).contains(value)) {
            throw new BusinessException(400, "Invalid reward status");
        }
        return value;
    }

    private RewardResponse toRewardResponse(Reward reward) {
        boolean available = ACTIVE.equals(reward.getStatus())
                && (reward.getStock() == null || safeInt(reward.getRedeemedCount()) < reward.getStock());
        return new RewardResponse(
                reward.getId(),
                reward.getTitle(),
                reward.getDescription(),
                reward.getCoverObjectKey(),
                reward.getCoverUrl(),
                reward.getCoinCost(),
                reward.getStock(),
                safeInt(reward.getRedeemedCount()),
                reward.getStatus(),
                reward.getSortOrder(),
                available,
                reward.getCreatedAt(),
                reward.getUpdatedAt()
        );
    }

    private RewardRedemptionResponse toRedemptionResponse(RewardRedemption redemption) {
        return new RewardRedemptionResponse(
                redemption.getId(),
                redemption.getRewardId(),
                redemption.getCoinCostSnapshot(),
                redemption.getRewardTitleSnapshot(),
                redemption.getRewardDescriptionSnapshot(),
                redemption.getRewardCoverUrlSnapshot(),
                redemption.getStatus(),
                redemption.getNote(),
                redemption.getCreatedAt(),
                redemption.getUpdatedAt()
        );
    }

    private void createRedeemedNotificationSafely(Long userId, Reward reward, RewardRedemption redemption) {
        try {
            notificationService.createNotification(userId, null, "REWARD_REDEEMED", "兑换成功",
                    "你已兑换「" + reward.getTitle() + "」，消耗 " + reward.getCoinCost() + " 专注币。",
                    REWARD_REDEMPTION, redemption.getId(), null,
                    Map.of("rewardId", reward.getId(), "rewardTitle", reward.getTitle(), "coinCost", reward.getCoinCost()));
        } catch (Exception ex) {
            log.warn("Create reward redeemed notification failed: {}", redemption.getId(), ex);
        }
    }

    private void createSoldOutNotificationsSafely(Reward reward) {
        for (Long adminUserId : adminAccessService.listRewardAdminUserIds()) {
            try {
                notificationService.createNotification(adminUserId, null, "REWARD_STOCK_SOLD_OUT", "奖励已兑完",
                        "「" + reward.getTitle() + "」库存已用尽。", "REWARD", reward.getId(), null,
                        Map.of("rewardId", reward.getId(), "rewardTitle", reward.getTitle()));
            } catch (Exception ex) {
                log.warn("Create reward sold out notification failed: {}", reward.getId(), ex);
            }
        }
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
