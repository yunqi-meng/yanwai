package com.yanwai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yanwai.entity.*;

import java.util.List;
import java.util.Map;

public interface AdminService {
    
    // 管理员认证
    Admin login(String username, String password);
    
    Admin getAdminById(Long adminId);
    
    // 数据看板
    Map<String, Object> getDashboardOverview();
    
    List<Map<String, Object>> getUserTrend(int days);
    
    List<Map<String, Object>> getAnalysisTrend(int days);
    
    List<Map<String, Object>> getHotAnalysis(int limit);
    
    // 用户管理
    Page<User> getUserList(int page, int size, String keyword, Integer memberLevel);
    
    User getUserDetail(Long userId);
    
    void updateUser(Long userId, User user);
    
    void banUser(Long userId);
    
    void unbanUser(Long userId);
    
    void updateMemberLevel(Long userId, Integer level, Integer days);
    
    // 卡牌管理
    Page<PersonalityCardDef> getCardList(int page, int size, Integer rarity);
    
    PersonalityCardDef getCardById(Long id);
    
    void createCard(PersonalityCardDef card);
    
    void updateCard(Long id, PersonalityCardDef card);
    
    void deleteCard(Long id);
    
    // 成就管理
    Page<AchievementDef> getAchievementList(int page, int size);
    
    AchievementDef getAchievementById(Long id);
    
    void createAchievement(AchievementDef achievement);
    
    void updateAchievement(Long id, AchievementDef achievement);
    
    void deleteAchievement(Long id);
    
    // 分析记录管理
    Page<AnalysisRecord> getAnalysisList(int page, int size, Long userId, String startDate, String endDate);
    
    AnalysisRecord getAnalysisById(Long id);
    
    void deleteAnalysis(Long id);
    
    // 系统配置
    List<SystemConfig> getAllConfigs();
    
    void updateConfig(String key, String value);
    
    SystemConfig getConfigByKey(String key);
    
    // 操作日志
    Page<OperationLog> getLogList(int page, int size, Long adminId, String startDate, String endDate);
    
    void saveLog(OperationLog log);
}
