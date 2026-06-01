package com.lifelink.reward.service;

import com.lifelink.reward.dto.RewardAdminAccessResponse;
import com.lifelink.reward.dto.RewardCoverUploadResponse;
import com.lifelink.reward.dto.RewardRedeemResponse;
import com.lifelink.reward.dto.RewardRedemptionResponse;
import com.lifelink.reward.dto.RewardRequest;
import com.lifelink.reward.dto.RewardResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface RewardService {

    List<RewardResponse> listRewards(String status, String keyword, String sortBy, String sortDirection, Integer page, Integer pageSize, Long userId);

    RewardResponse getReward(Long id, Long userId);

    RewardRedeemResponse redeem(Long id, Long userId);

    List<RewardRedemptionResponse> listMyRedemptions(Integer page, Integer pageSize, Long userId);

    RewardAdminAccessResponse getAdminAccess(Long userId);

    RewardResponse createReward(RewardRequest request, Long userId);

    RewardResponse updateReward(Long id, RewardRequest request, Long userId);

    void deactivateReward(Long id, Long userId);

    RewardCoverUploadResponse uploadCover(MultipartFile file, Long userId);
}
