package com.lifelink.background.service;

import com.lifelink.background.dto.SaveUserBackgroundSettingRequest;
import com.lifelink.background.dto.UserBackgroundSettingResponse;
import com.lifelink.background.dto.UserBackgroundUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserBackgroundSettingService {

    UserBackgroundSettingResponse getCurrentSetting(Long userId);

    UserBackgroundUploadResponse uploadBackground(Long userId, MultipartFile file);

    UserBackgroundSettingResponse saveCurrentSetting(Long userId, SaveUserBackgroundSettingRequest request);

    UserBackgroundSettingResponse resetCurrentSetting(Long userId);
}
