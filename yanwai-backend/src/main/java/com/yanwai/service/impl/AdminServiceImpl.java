package com.yanwai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yanwai.config.AiProperties;
import com.yanwai.config.GameProperties;
import com.yanwai.entity.*;
import com.yanwai.mapper.*;
import com.yanwai.service.AdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    private final AdminMapper adminMapper;
    private final UserMapper userMapper;
    private final PersonalityCardDefMapper cardMapper;
    private final AchievementDefMapper achievementMapper;
    private final AnalysisRecordMapper analysisMapper;
    private final SystemConfigMapper configMapper;
    private final OperationLogMapper logMapper;
    private final AiProperties aiProperties;
    private final GameProperties gameProperties;
    private final PasswordEncoder passwordEncoder;

    public AdminServiceImpl(AdminMapper adminMapper, UserMapper userMapper,
                           PersonalityCardDefMapper cardMapper, AchievementDefMapper achievementMapper,
                           AnalysisRecordMapper analysisMapper, SystemConfigMapper configMapper,
                           OperationLogMapper logMapper,
                           AiProperties aiProperties, GameProperties gameProperties,
                           PasswordEncoder passwordEncoder) {
        this.adminMapper = adminMapper;
        this.userMapper = userMapper;
        this.cardMapper = cardMapper;
        this.achievementMapper = achievementMapper;
        this.analysisMapper = analysisMapper;
        this.configMapper = configMapper;
        this.logMapper = logMapper;
        this.aiProperties = aiProperties;
        this.gameProperties = gameProperties;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Admin login(String username, String password) {
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Admin::getUsername, username);
        Admin admin = adminMapper.selectOne(wrapper);
        
        if (admin != null && verifyPassword(password, admin.getPassword())) {
            admin.setLastLoginTime(LocalDateTime.now());
            adminMapper.updateById(admin);
            admin.setPassword(null);
            return admin;
        }
        return null;
    }

    @Override
    public Admin getAdminById(Long adminId) {
        Admin admin = adminMapper.selectById(adminId);
        if (admin != null) {
            admin.setPassword(null);
        }
        return admin;
    }

    @Override
    public Map<String, Object> getDashboardOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        // 总用户数
        overview.put("totalUsers", userMapper.selectCount(null));
        
        // 今日新增用户
        LambdaQueryWrapper<User> todayUserWrapper = new LambdaQueryWrapper<>();
        todayUserWrapper.ge(User::getCreatedAt, LocalDate.now().atStartOfDay());
        overview.put("todayNewUsers", userMapper.selectCount(todayUserWrapper));
        
        // 总分析次数
        overview.put("totalAnalysis", analysisMapper.selectCount(null));
        
        // 今日分析次数
        LambdaQueryWrapper<AnalysisRecord> todayAnalysisWrapper = new LambdaQueryWrapper<>();
        todayAnalysisWrapper.ge(AnalysisRecord::getCreatedAt, LocalDate.now().atStartOfDay());
        overview.put("todayAnalysis", analysisMapper.selectCount(todayAnalysisWrapper));
        
        // 会员用户数
        LambdaQueryWrapper<User> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(User::getMemberLevel, 1);
        overview.put("memberUsers", userMapper.selectCount(memberWrapper));
        
        // 总卡牌数
        overview.put("totalCards", cardMapper.selectCount(null));
        
        // 总成就数
        overview.put("totalAchievements", achievementMapper.selectCount(null));
        
        return overview;
    }

    @Override
    public List<Map<String, Object>> getUserTrend(int days) {
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDate startDate = LocalDate.now().minusDays(days);
        
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.toString());
            
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.ge(User::getCreatedAt, date.atStartOfDay())
                   .lt(User::getCreatedAt, date.plusDays(1).atStartOfDay());
            dayData.put("count", userMapper.selectCount(wrapper));
            
            trend.add(dayData);
        }
        return trend;
    }

    @Override
    public List<Map<String, Object>> getAnalysisTrend(int days) {
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDate startDate = LocalDate.now().minusDays(days);
        
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.toString());
            
            LambdaQueryWrapper<AnalysisRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.ge(AnalysisRecord::getCreatedAt, date.atStartOfDay())
                   .lt(AnalysisRecord::getCreatedAt, date.plusDays(1).atStartOfDay());
            dayData.put("count", analysisMapper.selectCount(wrapper));
            
            trend.add(dayData);
        }
        return trend;
    }

    @Override
    public List<Map<String, Object>> getHotAnalysis(int limit) {
        List<Map<String, Object>> hotList = new ArrayList<>();
        LambdaQueryWrapper<AnalysisRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(AnalysisRecord::getCreatedAt).last("LIMIT " + limit);
        
        List<AnalysisRecord> records = analysisMapper.selectList(wrapper);
        for (AnalysisRecord record : records) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", record.getId());
            item.put("userId", record.getUserId());
            item.put("originalText", record.getOriginalText());
            item.put("relationship", record.getRelationship());
            item.put("createdAt", record.getCreatedAt());
            
            // 获取用户昵称
            User user = userMapper.selectById(record.getUserId());
            if (user != null) {
                item.put("nickname", user.getNickname());
            }
            hotList.add(item);
        }
        return hotList;
    }

    @Override
    public Page<User> getUserList(int page, int size, String keyword, Integer memberLevel) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getNickname, keyword)
                           .or().like(User::getOpenid, keyword)
                           .or().like(User::getEmail, keyword));
        }
        if (memberLevel != null) {
            wrapper.eq(User::getMemberLevel, memberLevel);
        }
        wrapper.orderByDesc(User::getCreatedAt);
        
        return userMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public User getUserDetail(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    @Transactional
    public void updateUser(Long userId, User user) {
        user.setId(userId);
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void banUser(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setMemberLevel(-1); // -1表示封禁
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void unbanUser(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setMemberLevel(0);
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void updateMemberLevel(Long userId, Integer level, Integer days) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setMemberLevel(level);
            if (level == 1 && days != null && days > 0) {
                user.setMemberExpireTime(LocalDateTime.now().plusDays(days));
            }
            userMapper.updateById(user);
        }
    }

    @Override
    public Page<PersonalityCardDef> getCardList(int page, int size, Integer rarity) {
        Page<PersonalityCardDef> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<PersonalityCardDef> wrapper = new LambdaQueryWrapper<>();
        
        if (rarity != null) {
            wrapper.eq(PersonalityCardDef::getRarity, rarity);
        }
        wrapper.orderByAsc(PersonalityCardDef::getRarity)
               .orderByAsc(PersonalityCardDef::getId);
        
        return cardMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public PersonalityCardDef getCardById(Long id) {
        return cardMapper.selectById(id);
    }

    @Override
    @Transactional
    public void createCard(PersonalityCardDef card) {
        cardMapper.insert(card);
    }

    @Override
    @Transactional
    public void updateCard(Long id, PersonalityCardDef card) {
        card.setId(id);
        cardMapper.updateById(card);
    }

    @Override
    @Transactional
    public void deleteCard(Long id) {
        cardMapper.deleteById(id);
    }

    @Override
    public Page<AchievementDef> getAchievementList(int page, int size) {
        Page<AchievementDef> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<AchievementDef> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(AchievementDef::getId);
        return achievementMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public AchievementDef getAchievementById(Long id) {
        return achievementMapper.selectById(id);
    }

    @Override
    @Transactional
    public void createAchievement(AchievementDef achievement) {
        achievementMapper.insert(achievement);
    }

    @Override
    @Transactional
    public void updateAchievement(Long id, AchievementDef achievement) {
        achievement.setId(id);
        achievementMapper.updateById(achievement);
    }

    @Override
    @Transactional
    public void deleteAchievement(Long id) {
        achievementMapper.deleteById(id);
    }

    @Override
    public Page<AnalysisRecord> getAnalysisList(int page, int size, Long userId, String startDate, String endDate) {
        Page<AnalysisRecord> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<AnalysisRecord> wrapper = new LambdaQueryWrapper<>();
        
        if (userId != null) {
            wrapper.eq(AnalysisRecord::getUserId, userId);
        }
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(AnalysisRecord::getCreatedAt, LocalDateTime.parse(startDate + "T00:00:00"));
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(AnalysisRecord::getCreatedAt, LocalDateTime.parse(endDate + "T23:59:59"));
        }
        wrapper.orderByDesc(AnalysisRecord::getCreatedAt);
        
        Page<AnalysisRecord> result = analysisMapper.selectPage(pageParam, wrapper);
        result.getRecords().forEach(record -> record.setOriginalImage(null));
        return result;
    }

    @Override
    public AnalysisRecord getAnalysisById(Long id) {
        return analysisMapper.selectById(id);
    }

    @Override
    @Transactional
    public void deleteAnalysis(Long id) {
        analysisMapper.deleteById(id);
    }

    @Override
    public List<SystemConfig> getAllConfigs() {
        List<SystemConfig> dbConfigs = configMapper.selectList(null);
        Map<String, String> dbMap = dbConfigs.stream()
                .collect(Collectors.toMap(SystemConfig::getConfigKey, SystemConfig::getConfigValue));

        Map<String, String> defaults = new LinkedHashMap<>();
        defaults.put("yanwai.ai.api-key", aiProperties.getApiKey() != null ? aiProperties.getApiKey() : "");
        defaults.put("yanwai.ai.base-url", aiProperties.getBaseUrl() != null ? aiProperties.getBaseUrl() : "https://api.deepseek.com");
        defaults.put("yanwai.ai.model", aiProperties.getModel() != null ? aiProperties.getModel() : "deepseek-chat");
        defaults.put("yanwai.ai.vision-api-key", aiProperties.getVisionApiKey() != null ? aiProperties.getVisionApiKey() : "");
        defaults.put("yanwai.ai.vision-base-url", aiProperties.getVisionBaseUrl() != null ? aiProperties.getVisionBaseUrl() : "https://ark.cn-beijing.volces.com/api/v3");
        defaults.put("yanwai.ai.vision-model", aiProperties.getVisionModel() != null ? aiProperties.getVisionModel() : "doubao-seed-1-6-vision-250815");
        defaults.put("yanwai.ai.enable-mock", String.valueOf(aiProperties.getEnableMock() != null && aiProperties.getEnableMock()));
        defaults.put("yanwai.game.card-drop-probability", String.valueOf((int)(gameProperties.getCardDropProbability() * 100)));
        defaults.put("yanwai.game.free-daily-analysis", String.valueOf(gameProperties.getFreeDailyAnalysis()));
        defaults.put("yanwai.member.ad-duration", "30");
        defaults.put("yanwai.member.legend-probability-normal", "5");
        defaults.put("yanwai.member.legend-probability-member", "10");

        List<SystemConfig> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : defaults.entrySet()) {
            SystemConfig config = new SystemConfig();
            config.setConfigKey(entry.getKey());
            config.setConfigValue(dbMap.getOrDefault(entry.getKey(), entry.getValue()));
            result.add(config);
        }
        return result;
    }

    @Override
    @Transactional
    public void updateConfig(String key, String value) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, key);
        SystemConfig config = configMapper.selectOne(wrapper);
        
        if (config != null) {
            config.setConfigValue(value);
            configMapper.updateById(config);
        } else {
            config = new SystemConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            configMapper.insert(config);
        }
    }

    @Override
    public SystemConfig getConfigByKey(String key) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, key);
        return configMapper.selectOne(wrapper);
    }

    @Override
    public Page<OperationLog> getLogList(int page, int size, Long adminId, String startDate, String endDate) {
        Page<OperationLog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        
        if (adminId != null) {
            wrapper.eq(OperationLog::getAdminId, adminId);
        }
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(OperationLog::getCreatedAt, LocalDateTime.parse(startDate + "T00:00:00"));
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(OperationLog::getCreatedAt, LocalDateTime.parse(endDate + "T23:59:59"));
        }
        wrapper.orderByDesc(OperationLog::getCreatedAt);
        
        return logMapper.selectPage(pageParam, wrapper);
    }

    @Override
    @Transactional
    public void saveLog(OperationLog log) {
        logMapper.insert(log);
    }

    private boolean verifyPassword(String rawPassword, String encryptedPassword) {
        return passwordEncoder.matches(rawPassword, encryptedPassword);
    }
}
