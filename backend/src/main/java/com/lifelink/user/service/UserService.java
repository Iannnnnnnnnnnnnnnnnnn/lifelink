package com.lifelink.user.service;

import com.lifelink.user.dto.LoginRequest;
import com.lifelink.user.dto.LoginResponse;
import com.lifelink.user.dto.RegisterRequest;
import com.lifelink.user.dto.UserProfileResponse;

public interface UserService {

    UserProfileResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    UserProfileResponse getCurrentUser(Long userId);
}
