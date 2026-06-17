package com.lifelink.background.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifelink.background.dto.SaveUserBackgroundSettingRequest;
import com.lifelink.background.dto.UserBackgroundSettingResponse;
import com.lifelink.background.dto.UserBackgroundUploadResponse;
import com.lifelink.background.entity.UserBackgroundSetting;
import com.lifelink.background.mapper.UserBackgroundSettingMapper;
import com.lifelink.background.service.UserBackgroundSettingService;
import com.lifelink.common.BusinessException;
import com.lifelink.file.dto.FileUploadResponse;
import com.lifelink.file.entity.FileResource;
import com.lifelink.file.mapper.FileResourceMapper;
import com.lifelink.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserBackgroundSettingServiceImpl implements UserBackgroundSettingService {

    private static final String GLOBAL_SCOPE = "GLOBAL";
    private static final double DEFAULT_SCALE = 1.0;
    private static final int DEFAULT_POSITION = 50;
    private static final String DEFAULT_PRESET_POSITION = "CENTER";
    private static final double DEFAULT_OPACITY = 0.22;
    private static final int DEFAULT_BLUR = 0;
    private static final double DEFAULT_OVERLAY_OPACITY = 0.35;
    private static final Set<String> ALLOWED_PRESET_POSITIONS = new HashSet<String>(Arrays.asList(
            "CENTER",
            "TOP",
            "BOTTOM",
            "LEFT",
            "RIGHT",
            "TOP_LEFT",
            "TOP_RIGHT",
            "BOTTOM_LEFT",
            "BOTTOM_RIGHT"
    ));

    private final UserBackgroundSettingMapper userBackgroundSettingMapper;

    private final FileResourceMapper fileResourceMapper;

    private final FileService fileService;

    @Override
    public UserBackgroundSettingResponse getCurrentSetting(Long userId) {
        UserBackgroundSetting setting = findSetting(userId, GLOBAL_SCOPE);
        if (setting == null) {
            return defaultResponse();
        }
        return toResponse(setting);
    }

    @Override
    @Transactional
    public UserBackgroundUploadResponse uploadBackground(Long userId, MultipartFile file) {
        FileUploadResponse uploadResponse = fileService.uploadBackgroundImage(file, userId);
        return new UserBackgroundUploadResponse(uploadResponse.getUrl(), uploadResponse.getObjectKey());
    }

    @Override
    @Transactional
    public UserBackgroundSettingResponse saveCurrentSetting(Long userId, SaveUserBackgroundSettingRequest request) {
        if (request == null) {
            throw new BusinessException(400, "Background setting is required");
        }
        String scope = normalizeScope(request.getScope());
        String presetPosition = normalizePresetPosition(request.getPresetPosition());
        String objectKey = trimToNull(request.getObjectKey());

        UserBackgroundSetting existing = findSetting(userId, scope);
        String resolvedObjectKey = objectKey != null ? objectKey : existing == null ? null : existing.getObjectKey();
        String resolvedImageUrl = existing == null ? null : existing.getImageUrl();

        if (resolvedObjectKey != null) {
            FileResource fileResource = findOwnedBackgroundFile(userId, resolvedObjectKey);
            if (fileResource == null) {
                throw new BusinessException(400, "Background image is invalid or not uploaded by current user");
            }
            resolvedImageUrl = fileResource.getFileUrl();
        }

        boolean enabled = request.getEnabled() != null && request.getEnabled();
        if (enabled && !StringUtils.hasText(resolvedObjectKey)) {
            throw new BusinessException(400, "Background image is required");
        }

        LocalDateTime now = LocalDateTime.now();
        UserBackgroundSetting setting = existing == null ? new UserBackgroundSetting() : existing;
        setting.setUserId(userId);
        setting.setEnabled(enabled);
        setting.setObjectKey(resolvedObjectKey);
        setting.setImageUrl(resolvedImageUrl);
        setting.setScale(defaultIfNull(request.getScale(), DEFAULT_SCALE));
        setting.setPositionX(defaultIfNull(request.getPositionX(), DEFAULT_POSITION));
        setting.setPositionY(defaultIfNull(request.getPositionY(), DEFAULT_POSITION));
        setting.setPresetPosition(presetPosition);
        setting.setOpacity(defaultIfNull(request.getOpacity(), DEFAULT_OPACITY));
        setting.setBlur(defaultIfNull(request.getBlur(), DEFAULT_BLUR));
        setting.setOverlayOpacity(defaultIfNull(request.getOverlayOpacity(), DEFAULT_OVERLAY_OPACITY));
        setting.setScope(scope);
        setting.setUpdatedAt(now);

        if (setting.getId() == null) {
            setting.setCreatedAt(now);
            userBackgroundSettingMapper.insert(setting);
        } else {
            userBackgroundSettingMapper.updateById(setting);
        }
        return toResponse(setting);
    }

    @Override
    @Transactional
    public UserBackgroundSettingResponse resetCurrentSetting(Long userId) {
        UserBackgroundSetting setting = findSetting(userId, GLOBAL_SCOPE);
        if (setting == null) {
            return defaultResponse();
        }
        setting.setEnabled(false);
        setting.setUpdatedAt(LocalDateTime.now());
        userBackgroundSettingMapper.updateById(setting);
        return toResponse(setting);
    }

    private UserBackgroundSetting findSetting(Long userId, String scope) {
        return userBackgroundSettingMapper.selectOne(new LambdaQueryWrapper<UserBackgroundSetting>()
                .eq(UserBackgroundSetting::getUserId, userId)
                .eq(UserBackgroundSetting::getScope, scope)
                .last("LIMIT 1"));
    }

    private FileResource findOwnedBackgroundFile(Long userId, String objectKey) {
        return fileResourceMapper.selectOne(new LambdaQueryWrapper<FileResource>()
                .eq(FileResource::getUserId, userId)
                .eq(FileResource::getObjectKey, objectKey)
                .likeRight(FileResource::getObjectKey, "backgrounds/" + userId + "/")
                .last("LIMIT 1"));
    }

    private String normalizeScope(String scope) {
        if (!StringUtils.hasText(scope)) {
            return GLOBAL_SCOPE;
        }
        String normalized = scope.trim().toUpperCase();
        if (!GLOBAL_SCOPE.equals(normalized)) {
            throw new BusinessException(400, "Only GLOBAL background scope is supported");
        }
        return normalized;
    }

    private String normalizePresetPosition(String presetPosition) {
        if (!StringUtils.hasText(presetPosition)) {
            return DEFAULT_PRESET_POSITION;
        }
        String normalized = presetPosition.trim().toUpperCase();
        if (!ALLOWED_PRESET_POSITIONS.contains(normalized)) {
            throw new BusinessException(400, "Unsupported background position");
        }
        return normalized;
    }

    private UserBackgroundSettingResponse defaultResponse() {
        return new UserBackgroundSettingResponse(
                false,
                null,
                null,
                DEFAULT_SCALE,
                DEFAULT_POSITION,
                DEFAULT_POSITION,
                DEFAULT_PRESET_POSITION,
                DEFAULT_OPACITY,
                DEFAULT_BLUR,
                DEFAULT_OVERLAY_OPACITY,
                GLOBAL_SCOPE
        );
    }

    private UserBackgroundSettingResponse toResponse(UserBackgroundSetting setting) {
        return new UserBackgroundSettingResponse(
                setting.getEnabled(),
                setting.getImageUrl(),
                setting.getObjectKey(),
                setting.getScale(),
                setting.getPositionX(),
                setting.getPositionY(),
                setting.getPresetPosition(),
                setting.getOpacity(),
                setting.getBlur(),
                setting.getOverlayOpacity(),
                setting.getScope()
        );
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private <T> T defaultIfNull(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }
}
