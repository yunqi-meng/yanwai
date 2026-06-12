package com.yanwai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yanwai.entity.User;
import com.yanwai.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setNickname("测试用户");
        testUser.setMemberLevel(0);
        testUser.setDailyAnalysisCount(1);
        testUser.setTotalAnalysis(10);
        testUser.setTotalShare(5);
        testUser.setMemberExpireTime(null);
    }

    @Test
    void login_Success() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "test@example.com");
        loginRequest.put("password", "password123");

        when(userService.login("test@example.com", "password123")).thenReturn(testUser);
        when(userService.canWatchAd(1L)).thenReturn(true);

        mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.nickname").value("测试用户"))
                .andExpect(jsonPath("$.data.memberLevel").value(0));
    }

    @Test
    void login_InvalidCredentials() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "test@example.com");
        loginRequest.put("password", "wrongpassword");

        when(userService.login("test@example.com", "wrongpassword")).thenReturn(null);

        mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("邮箱或密码错误"));
    }

    @Test
    void login_EmptyEmail() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "");
        loginRequest.put("password", "password123");

        mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("邮箱不能为空"));
    }

    @Test
    void getStats_Success() throws Exception {
        when(userService.getUserStats(1L)).thenReturn(testUser);
        when(userService.canWatchAd(1L)).thenReturn(true);

        mockMvc.perform(get("/api/user/stats")
                .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.nickname").value("测试用户"));
    }

    @Test
    void canWatchAd_ReturnsTrue() throws Exception {
        when(userService.canWatchAd(1L)).thenReturn(true);

        mockMvc.perform(get("/api/user/can-watch-ad")
                .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }
}
