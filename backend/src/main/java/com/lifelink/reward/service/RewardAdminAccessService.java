package com.lifelink.reward.service;

import com.lifelink.common.BusinessException;
import com.lifelink.config.RewardProperties;
import com.lifelink.user.entity.User;
import com.lifelink.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class RewardAdminAccessService {

    public static final String ACCESS_DENIED_MESSAGE = "你没有奖励管理权限";

    private final UserMapper userMapper;
    private final Set<String> adminPhones;

    public RewardAdminAccessService(UserMapper userMapper, RewardProperties properties) {
        this.userMapper = userMapper;
        this.adminPhones = normalizePhones(properties.getAdminPhones());
    }

    public boolean isRewardAdmin(Long userId) {
        if (userId == null || adminPhones.isEmpty()) {
            return false;
        }
        User user = userMapper.selectById(userId);
        return user != null && adminPhones.contains(normalizePhone(user.getPhone()));
    }

    public void requireRewardAdmin(Long userId) {
        if (!isRewardAdmin(userId)) {
            throw new BusinessException(403, ACCESS_DENIED_MESSAGE);
        }
    }

    public List<Long> listRewardAdminUserIds() {
        if (adminPhones.isEmpty()) {
            return List.of();
        }
        return userMapper.selectList(null).stream()
                .filter(user -> adminPhones.contains(normalizePhone(user.getPhone())))
                .map(User::getId)
                .toList();
    }

    public String normalizePhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return "";
        }
        String normalized = phone.trim().replaceAll("[\\s-]", "");
        if (normalized.startsWith("+86")) {
            normalized = normalized.substring(3);
        } else if (normalized.startsWith("86") && normalized.length() == 13) {
            normalized = normalized.substring(2);
        }
        if (normalized.length() != 11) {
            return "";
        }
        for (int i = 0; i < normalized.length(); i++) {
            if (!Character.isDigit(normalized.charAt(i))) {
                return "";
            }
        }
        return normalized;
    }

    private Set<String> normalizePhones(String phones) {
        if (!StringUtils.hasText(phones)) {
            return Collections.emptySet();
        }
        Set<String> result = new LinkedHashSet<String>();
        for (String phone : phones.split(",")) {
            String normalized = normalizePhone(phone);
            if (StringUtils.hasText(normalized)) {
                result.add(normalized);
            }
        }
        return Collections.unmodifiableSet(result);
    }
}
