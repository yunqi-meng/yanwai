package com.yanwai.service;

import com.yanwai.entity.*;

import java.util.List;
import java.util.Map;

public interface SocialService {
    
    // ===== 签到系统 =====
    Map<String, Object> checkin(Long userId);
    Map<String, Object> getCheckinStatus(Long userId);
    List<Map<String, Object>> getCheckinRanking(Integer limit);
    
    // ===== 好友系统 =====
    void sendFriendRequest(Long fromUserId, Long toUserId, String message);
    void handleFriendRequest(Long requestId, Long userId, boolean accept);
    List<Map<String, Object>> getPendingRequests(Long userId);
    List<Map<String, Object>> getFriendList(Long userId);
    void removeFriend(Long userId, Long friendId);
    
    // ===== 排行榜 =====
    List<Map<String, Object>> getCardRanking(Integer limit);
    List<Map<String, Object>> getAnalysisRanking(Integer limit);
    List<Map<String, Object>> getAchievementRanking(Integer limit);
    
    // ===== 回复建议 =====
    Map<String, Object> generateReplySuggestion(Long analysisId, Long userId);
    Map<String, Object> getReplySuggestion(Long analysisId);
}
