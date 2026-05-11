package com.lifelink.anniversary.controller;

import com.lifelink.anniversary.dto.AnniversaryDetailResponse;
import com.lifelink.anniversary.dto.AnniversaryResponse;
import com.lifelink.anniversary.dto.CreateAnniversaryRequest;
import com.lifelink.anniversary.dto.UpdateAnniversaryRequest;
import com.lifelink.anniversary.service.AnniversaryService;
import com.lifelink.common.Result;
import com.lifelink.security.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

import java.util.List;

@RestController
@RequestMapping("/api/anniversaries")
@RequiredArgsConstructor
public class AnniversaryController {

    private final AnniversaryService anniversaryService;

    @PostMapping
    public Result<AnniversaryDetailResponse> createAnniversary(
            @Valid @RequestBody CreateAnniversaryRequest request,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(anniversaryService.createAnniversary(request, loginUser.getId()));
    }

    @GetMapping
    public Result<List<AnniversaryResponse>> listAnniversaries(
            @RequestParam(required = false) Long relationshipId,
            @RequestParam(required = false) String repeatType,
            @RequestParam(required = false) String displayType,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(anniversaryService.listAnniversaries(relationshipId, repeatType, displayType, keyword, page, size, loginUser.getId()));
    }

    @GetMapping("/{id}")
    public Result<AnniversaryDetailResponse> getAnniversaryDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(anniversaryService.getAnniversaryDetail(id, loginUser.getId()));
    }

    @PutMapping("/{id}")
    public Result<AnniversaryDetailResponse> updateAnniversary(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAnniversaryRequest request,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(anniversaryService.updateAnniversary(id, request, loginUser.getId()));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteAnniversary(
            @PathVariable Long id,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        anniversaryService.deleteAnniversary(id, loginUser.getId());
        return Result.success();
    }
}
