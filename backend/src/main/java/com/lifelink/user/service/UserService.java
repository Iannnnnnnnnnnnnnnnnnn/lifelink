package com.lifelink.user.service;

import com.lifelink.user.dto.LoginRequest;
import com.lifelink.user.dto.LoginResponse;
import com.lifelink.user.dto.RegisterRequest;
import com.lifelink.user.dto.AvatarUploadResponse;
import com.lifelink.user.dto.ChangePasswordRequest;
import com.lifelink.user.dto.ChangePasswordResponse;
import com.lifelink.user.dto.UpdateUserProfileRequest;
import com.lifelink.user.dto.UserProfileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    UserProfileResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    UserProfileResponse getCurrentUser(Long userId);

    UserProfileResponse updateCurrentUser(Long userId, UpdateUserProfileRequest request);

    AvatarUploadResponse uploadAvatar(Long userId, MultipartFile file);

    ChangePasswordResponse changePassword(Long userId, ChangePasswordRequest request);
}
