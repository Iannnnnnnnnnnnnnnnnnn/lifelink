package com.lifelink.philosophy.service;

import com.lifelink.common.BusinessException;
import com.lifelink.config.PhilosophyAccessProperties;
import com.lifelink.user.entity.User;
import com.lifelink.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class PhilosophyAccessService {

    public static final String ACCESS_DENIED_MESSAGE = "思想对话功能暂未对当前账号开放";

    private final UserMapper userMapper;
    private final Set<String> allowedPhones;

    public PhilosophyAccessService(
            UserMapper userMapper,
            PhilosophyAccessProperties properties
    ) {
        this.userMapper = userMapper;
        this.allowedPhones = normalizeAllowedPhones(properties.getAllowedPhones());
    }

    public boolean canAccess(Long userId) {
        if (userId == null || allowedPhones.isEmpty()) {
            return false;
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        return allowedPhones.contains(normalizePhone(user.getPhone()));
    }

    public void requireAccess(Long userId) {
        if (!canAccess(userId)) {
            throw new BusinessException(403, ACCESS_DENIED_MESSAGE);
        }
    }

    private Set<String> normalizeAllowedPhones(String phones) {
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

    private String normalizePhone(String phone) {
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
}
