package com.yanwai.service;

import com.yanwai.entity.User;

public interface UserService {
    User loginOrCreateUser(String openid);
    
    User login(String email, String password);
    
    User register(String email, String password);
    
    String validateEmail(String email);
    
    String validatePassword(String password);
    
    User getUserById(Long userId);
    
    void resetUserData(Long userId);
    
    void incrementShareCount(Long userId);
    
    void updateDailyAnalysisCount(Long userId);
    
    boolean canDoAnalysis(Long userId);
    
    boolean watchAd(Long userId);
    
    boolean canWatchAd(Long userId);
    
    boolean updateUserInfo(Long userId, String nickname, String avatar);
}
