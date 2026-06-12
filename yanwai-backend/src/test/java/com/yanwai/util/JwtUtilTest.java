package com.yanwai.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "test-secret-key-must-be-at-least-256-bits-long-for-hmac-sha");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
    }

    @Test
    void generateToken_Success() {
        String token = jwtUtil.generateToken(1L, "admin", "ADMIN");
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void parseToken_Success() {
        String token = jwtUtil.generateToken(1L, "admin", "ADMIN");
        
        Long adminId = jwtUtil.getAdminIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);
        
        assertEquals(1L, adminId);
        assertEquals("admin", username);
        assertEquals("ADMIN", role);
    }

    @Test
    void validateToken_ValidToken() {
        String token = jwtUtil.generateToken(1L, "admin", "ADMIN");
        
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void validateToken_InvalidToken() {
        assertFalse(jwtUtil.validateToken("invalid-token"));
    }

    @Test
    void isTokenExpired_NotExpired() {
        String token = jwtUtil.generateToken(1L, "admin", "ADMIN");
        
        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    void isTokenExpired_Expired() {
        // 设置过期时间为0，立即过期
        ReflectionTestUtils.setField(jwtUtil, "expiration", 0L);
        String token = jwtUtil.generateToken(1L, "admin", "ADMIN");
        
        // 等待一小段时间确保token过期
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        assertTrue(jwtUtil.isTokenExpired(token));
    }

    @Test
    void parseToken_InvalidToken_ThrowsException() {
        assertThrows(Exception.class, () -> jwtUtil.parseToken("invalid-token"));
    }

    @Test
    void generateToken_DifferentUsers_DifferentTokens() {
        String token1 = jwtUtil.generateToken(1L, "admin1", "ADMIN");
        String token2 = jwtUtil.generateToken(2L, "admin2", "USER");
        
        assertNotEquals(token1, token2);
    }

    @Test
    void getAdminIdFromToken_Success() {
        String token = jwtUtil.generateToken(123L, "admin", "ADMIN");
        
        assertEquals(123L, jwtUtil.getAdminIdFromToken(token));
    }

    @Test
    void getUsernameFromToken_Success() {
        String token = jwtUtil.generateToken(1L, "testuser", "ADMIN");
        
        assertEquals("testuser", jwtUtil.getUsernameFromToken(token));
    }

    @Test
    void getRoleFromToken_Success() {
        String token = jwtUtil.generateToken(1L, "admin", "SUPER_ADMIN");
        
        assertEquals("SUPER_ADMIN", jwtUtil.getRoleFromToken(token));
    }
}
