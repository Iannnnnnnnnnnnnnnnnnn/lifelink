package com.lifelink.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifelink.user.entity.User;
import com.lifelink.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .last("LIMIT 1"));
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return new LoginUser(user.getId(), user.getUsername(), user.getPasswordHash(), user.getStatus());
    }
}
