package com.yanwai.service;

import com.yanwai.entity.User;

public interface UserService {
    User loginOrCreateUser(String openid);
    
    User getUserById(Long userId);
    
    void resetUserData(Long userId);
    
    void incrementShareCount(Long userId);
    
    void updateDailyAnalysisCount(Long userId);
    
    boolean canDoAnalysis(Long userId);
    
    boolean watchAd(Long userId);
    
    boolean canWatchAd(Long userId);
}
