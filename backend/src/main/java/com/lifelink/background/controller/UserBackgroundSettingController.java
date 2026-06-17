package com.lifelink.background.controller;

import com.lifelink.background.dto.SaveUserBackgroundSettingRequest;
import com.lifelink.background.dto.UserBackgroundSettingResponse;
import com.lifelink.background.dto.UserBackgroundUploadResponse;
import com.lifelink.background.service.UserBackgroundSettingService;
import com.lifelink.common.Result;
import com.lifelink.security.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user-background")
@RequiredArgsConstructor
public class UserBackgroundSettingController {

    private final UserBackgroundSettingService userBackgroundSettingService;

    @GetMapping("/me")
    public Result<UserBackgroundSettingResponse> getCurrentSetting(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(userBackgroundSettingService.getCurrentSetting(loginUser.getId()));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<UserBackgroundUploadResponse> uploadBackground(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(userBackgroundSettingService.uploadBackground(loginUser.getId(), file));
    }

    @PutMapping("/me")
    public Result<UserBackgroundSettingResponse> saveCurrentSetting(
            @Valid @RequestBody SaveUserBackgroundSettingRequest request,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(userBackgroundSettingService.saveCurrentSetting(loginUser.getId(), request));
    }

    @DeleteMapping("/me")
    public Result<UserBackgroundSettingResponse> resetCurrentSetting(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(userBackgroundSettingService.resetCurrentSetting(loginUser.getId()));
    }
}
