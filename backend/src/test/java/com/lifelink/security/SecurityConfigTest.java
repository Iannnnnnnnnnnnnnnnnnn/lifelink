package com.lifelink.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelink.common.HealthController;
import com.lifelink.user.controller.AuthController;
import com.lifelink.user.controller.UserController;
import com.lifelink.user.dto.LoginResponse;
import com.lifelink.user.dto.UserProfileResponse;
import com.lifelink.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        AuthController.class,
        HealthController.class,
        UserController.class
})
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        UserProfileResponse user = new UserProfileResponse(
                1L,
                "alice",
                "alice@example.com",
                "13800000000",
                null,
                "ACTIVE",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(userService.register(any())).thenReturn(user);
        when(userService.login(any())).thenReturn(new LoginResponse("token", user));
    }

    @Test
    void registerShouldAllowAnonymousAccess() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "alice",
                                "email", "alice@example.com",
                                "password", "123456"
                        ))))
                .andExpect(status().isOk());
    }

    @Test
    void registerShouldAllowAnonymousAccessWhenApiPrefixIsStripped() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "alice",
                                "email", "alice@example.com",
                                "password", "123456"
                        ))))
                .andExpect(status().isOk());
    }

    @Test
    void loginShouldAllowAnonymousAccess() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "account", "alice",
                                "password", "123456"
                        ))))
                .andExpect(status().isOk());
    }

    @Test
    void loginShouldAllowAnonymousAccessWhenApiPrefixIsStripped() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "account", "alice",
                                "password", "123456"
                        ))))
                .andExpect(status().isOk());
    }

    @Test
    void healthShouldAllowAnonymousAccess() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }

    @Test
    void healthShouldAllowAnonymousAccessWhenApiPrefixIsStripped() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk());
    }

    @Test
    void businessApiShouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isUnauthorized());
    }
}
