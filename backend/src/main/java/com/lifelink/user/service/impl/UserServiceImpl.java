package com.lifelink.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifelink.common.BusinessException;
import com.lifelink.security.JwtUtil;
import com.lifelink.user.dto.LoginRequest;
import com.lifelink.user.dto.LoginResponse;
import com.lifelink.user.dto.RegisterRequest;
import com.lifelink.user.dto.UserProfileResponse;
import com.lifelink.user.entity.User;
import com.lifelink.user.mapper.UserMapper;
import com.lifelink.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String ACTIVE_STATUS = "ACTIVE";

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public UserProfileResponse register(RegisterRequest request) {
        String username = request.getUsername().trim();
        String email = trimToNull(request.getEmail());
        String phone = trimToNull(request.getPhone());

        ensureUnique("username", username, "Username already exists");
        if (email != null) {
            ensureUnique("email", email, "Email already exists");
        }
        if (phone != null) {
            ensureUnique("phone", phone, "Phone already exists");
        }

        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus(ACTIVE_STATUS);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userMapper.insert(user);

        return toProfile(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = findByAccount(request.getAccount().trim());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(401, "Invalid account or password");
        }
        if (!ACTIVE_STATUS.equals(user.getStatus())) {
            throw new BusinessException(403, "User account is disabled");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new LoginResponse(token, toProfile(user));
    }

    @Override
    public UserProfileResponse getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "User not found");
        }
        return toProfile(user);
    }

    private User findByAccount(String account) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, account)
                .or()
                .eq(User::getEmail, account)
                .or()
                .eq(User::getPhone, account)
                .last("LIMIT 1"));
    }

    private void ensureUnique(String field, String value, String message) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>();
        if ("username".equals(field)) {
            wrapper.eq(User::getUsername, value);
        } else if ("email".equals(field)) {
            wrapper.eq(User::getEmail, value);
        } else if ("phone".equals(field)) {
            wrapper.eq(User::getPhone, value);
        }
        Long count = userMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BusinessException(400, message);
        }
    }

    private UserProfileResponse toProfile(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatarUrl(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
