package com.lifelink.reward.controller;

import com.lifelink.common.Result;
import com.lifelink.reward.dto.RewardAdminAccessResponse;
import com.lifelink.reward.dto.RewardCoverUploadResponse;
import com.lifelink.reward.dto.RewardRedeemResponse;
import com.lifelink.reward.dto.RewardRedemptionResponse;
import com.lifelink.reward.dto.RewardRequest;
import com.lifelink.reward.dto.RewardResponse;
import com.lifelink.reward.service.RewardService;
import com.lifelink.security.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
public class RewardController {

    private final RewardService rewardService;

    @GetMapping
    public Result<List<RewardResponse>> listRewards(@RequestParam(required = false) String status,
                                                    @RequestParam(required = false) String keyword,
                                                    @RequestParam(required = false) String sortBy,
                                                    @RequestParam(required = false) String sortDirection,
                                                    @RequestParam(required = false) Integer page,
                                                    @RequestParam(required = false) Integer pageSize,
                                                    @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(rewardService.listRewards(status, keyword, sortBy, sortDirection, page, pageSize, loginUser.getId()));
    }

    @GetMapping("/redemptions/me")
    public Result<List<RewardRedemptionResponse>> myRedemptions(@RequestParam(required = false) Integer page,
                                                                @RequestParam(required = false) Integer pageSize,
                                                                @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(rewardService.listMyRedemptions(page, pageSize, loginUser.getId()));
    }

    @GetMapping("/admin/access")
    public Result<RewardAdminAccessResponse> adminAccess(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(rewardService.getAdminAccess(loginUser.getId()));
    }

    @PostMapping("/admin")
    public Result<RewardResponse> createReward(@Valid @RequestBody RewardRequest request,
                                               @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(rewardService.createReward(request, loginUser.getId()));
    }

    @PutMapping("/admin/{id}")
    public Result<RewardResponse> updateReward(@PathVariable Long id,
                                               @Valid @RequestBody RewardRequest request,
                                               @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(rewardService.updateReward(id, request, loginUser.getId()));
    }

    @DeleteMapping("/admin/{id}")
    public Result<Void> deleteReward(@PathVariable Long id, @AuthenticationPrincipal LoginUser loginUser) {
        rewardService.deactivateReward(id, loginUser.getId());
        return Result.success();
    }

    @PostMapping(value = "/admin/upload-cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<RewardCoverUploadResponse> uploadCover(@RequestParam("file") MultipartFile file,
                                                         @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(rewardService.uploadCover(file, loginUser.getId()));
    }

    @GetMapping("/{id}")
    public Result<RewardResponse> getReward(@PathVariable Long id, @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(rewardService.getReward(id, loginUser.getId()));
    }

    @PostMapping("/{id}/redeem")
    public Result<RewardRedeemResponse> redeem(@PathVariable Long id, @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(rewardService.redeem(id, loginUser.getId()));
    }
}
