package com.lifelink.user.controller;

import com.lifelink.common.Result;
import com.lifelink.security.LoginUser;
import com.lifelink.user.dto.UserProfileResponse;
import com.lifelink.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public Result<UserProfileResponse> me(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(userService.getCurrentUser(loginUser.getId()));
    }
}
