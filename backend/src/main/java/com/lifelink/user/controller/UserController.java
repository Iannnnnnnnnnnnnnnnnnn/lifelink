package com.lifelink.user.controller;

import com.lifelink.common.Result;
import com.lifelink.security.LoginUser;
import com.lifelink.user.dto.AvatarUploadResponse;
import com.lifelink.user.dto.ChangePasswordRequest;
import com.lifelink.user.dto.ChangePasswordResponse;
import com.lifelink.user.dto.UpdateUserProfileRequest;
import com.lifelink.user.dto.UserProfileResponse;
import com.lifelink.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping({"/api/user", "/api/users"})
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public Result<UserProfileResponse> me(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(userService.getCurrentUser(loginUser.getId()));
    }

    @PutMapping("/me")
    public Result<UserProfileResponse> updateMe(
            @Valid @RequestBody UpdateUserProfileRequest request,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(userService.updateCurrentUser(loginUser.getId(), request));
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<AvatarUploadResponse> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(userService.uploadAvatar(loginUser.getId(), file));
    }

    @PutMapping("/me/password")
    public Result<ChangePasswordResponse> changePassword(
            @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(userService.changePassword(loginUser.getId(), request));
    }
}
