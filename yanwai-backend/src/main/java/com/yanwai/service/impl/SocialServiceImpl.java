package com.yanwai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yanwai.entity.*;
import com.yanwai.mapper.*;
import com.yanwai.service.AIService;
import com.yanwai.service.SocialService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SocialServiceImpl implements SocialService {

    private final DailyCheckinMapper checkinMapper;
    private final FriendMapper friendMapper;
    private final FriendRequestMapper friendRequestMapper;
    private final ReplySuggestionMapper replySuggestionMapper;
    private final UserMapper userMapper;
    private final UserCardMapper userCardMapper;
    private final UserAchievementMapper userAchievementMapper;
    private final AnalysisRecordMapper analysisRecordMapper;
    private final AIService aiService;

    public SocialServiceImpl(DailyCheckinMapper checkinMapper, FriendMapper friendMapper,
                            FriendRequestMapper friendRequestMapper, ReplySuggestionMapper replySuggestionMapper,
                            UserMapper userMapper, UserCardMapper userCardMapper,
                            UserAchievementMapper userAchievementMapper, AnalysisRecordMapper analysisRecordMapper,
                            AIService aiService) {
        this.checkinMapper = checkinMapper;
        this.friendMapper = friendMapper;
        this.friendRequestMapper = friendRequestMapper;
        this.replySuggestionMapper = replySuggestionMapper;
        this.userMapper = userMapper;
        this.userCardMapper = userCardMapper;
        this.userAchievementMapper = userAchievementMapper;
        this.analysisRecordMapper = analysisRecordMapper;
        this.aiService = aiService;
    }

    // ==================== 签到系统 ====================
    
    @Override
    @Transactional
    public Map<String, Object> checkin(Long userId) {
        LocalDate today = LocalDate.now();
        DailyCheckin existing = checkinMapper.findByUserAndDate(userId, today);
        
        if (existing != null) {
            throw new RuntimeException("今日已签到");
        }
        
        // 计算连续签到天数
        LocalDate yesterday = today.minusDays(1);
        DailyCheckin yesterdayCheckin = checkinMapper.findByUserAndDate(userId, yesterday);
        int consecutiveDays = (yesterdayCheckin != null) ? yesterdayCheckin.getConsecutiveDays() + 1 : 1;
        
        // 生成奖励
        Map<String, String> reward = generateReward(consecutiveDays);
        
        DailyCheckin checkin = new DailyCheckin();
        checkin.setUserId(userId);
        checkin.setCheckinDate(today);
        checkin.setConsecutiveDays(consecutiveDays);
        checkin.setRewardType(reward.get("type"));
        checkin.setRewardValue(reward.get("value"));
        checkinMapper.insert(checkin);
        
        // 更新用户连续登录天数
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setLoginDays(consecutiveDays);
            userMapper.updateById(user);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("consecutiveDays", consecutiveDays);
        result.put("reward", reward);
        result.put("checkinDate", today);
        return result;
    }

    @Override
    public Map<String, Object> getCheckinStatus(Long userId) {
        LocalDate today = LocalDate.now();
        DailyCheckin todayCheckin = checkinMapper.findByUserAndDate(userId, today);
        
        // 获取最近30天签到记录
        List<DailyCheckin> recentCheckins = checkinMapper.findByUserIdDesc(userId, 30);
        List<LocalDate> checkinDates = recentCheckins.stream()
            .map(DailyCheckin::getCheckinDate)
            .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("checkedInToday", todayCheckin != null);
        result.put("consecutiveDays", todayCheckin != null ? todayCheckin.getConsecutiveDays() : 0);
        result.put("recentCheckinDates", checkinDates);
        result.put("totalCheckinDays", recentCheckins.size());
        return result;
    }

    @Override
    public List<Map<String, Object>> getCheckinRanking(Integer limit) {
        List<Map<String, Object>> ranking = checkinMapper.getConsecutiveRanking(limit);
        for (Map<String, Object> item : ranking) {
            Long userId = Long.valueOf(item.get("user_id").toString());
            User user = userMapper.selectById(userId);
            item.put("nickname", user != null ? user.getNickname() : "未知用户");
        }
        return ranking;
    }

    private Map<String, String> generateReward(int consecutiveDays) {
        Map<String, String> reward = new HashMap<>();
        if (consecutiveDays >= 7) {
            reward.put("type", "card");
            reward.put("value", "random_legend");
        } else if (consecutiveDays >= 3) {
            reward.put("type", "fragment");
            reward.put("value", "3");
        } else {
            reward.put("type", "analysis");
            reward.put("value", "1");
        }
        return reward;
    }

    // ==================== 好友系统 ====================
    
    @Override
    @Transactional
    public void sendFriendRequest(Long fromUserId, Long toUserId, String message) {
        if (fromUserId.equals(toUserId)) {
            throw new RuntimeException("不能添加自己为好友");
        }
        
        // 检查是否已是好友
        Friend existingFriend = friendMapper.findByUserAndFriend(fromUserId, toUserId);
        if (existingFriend != null) {
            throw new RuntimeException("已经是好友关系");
        }
        
        // 检查是否已有待处理请求
        FriendRequest existingRequest = friendRequestMapper.findExistingRequest(fromUserId, toUserId);
        if (existingRequest != null) {
            throw new RuntimeException("已发送过好友申请");
        }
        
        FriendRequest request = new FriendRequest();
        request.setFromUserId(fromUserId);
        request.setToUserId(toUserId);
        request.setMessage(message);
        request.setStatus(0);
        friendRequestMapper.insert(request);
    }

    @Override
    @Transactional
    public void handleFriendRequest(Long requestId, Long userId, boolean accept) {
        FriendRequest request = friendRequestMapper.selectById(requestId);
        if (request == null || !request.getToUserId().equals(userId)) {
            throw new RuntimeException("请求不存在");
        }
        
        if (accept) {
            request.setStatus(1);
            friendRequestMapper.updateById(request);
            
            // 创建双向好友关系
            Friend friend1 = new Friend();
            friend1.setUserId(request.getFromUserId());
            friend1.setFriendId(request.getToUserId());
            friend1.setStatus(1);
            friendMapper.insert(friend1);
            
            Friend friend2 = new Friend();
            friend2.setUserId(request.getToUserId());
            friend2.setFriendId(request.getFromUserId());
            friend2.setStatus(1);
            friendMapper.insert(friend2);
        } else {
            request.setStatus(2);
            friendRequestMapper.updateById(request);
        }
    }

    @Override
    public List<Map<String, Object>> getPendingRequests(Long userId) {
        List<FriendRequest> requests = friendRequestMapper.findPendingRequests(userId);
        return requests.stream().map(req -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", req.getId());
            map.put("fromUserId", req.getFromUserId());
            map.put("message", req.getMessage());
            map.put("createdAt", req.getCreatedAt());
            
            User fromUser = userMapper.selectById(req.getFromUserId());
            map.put("fromNickname", fromUser != null ? fromUser.getNickname() : "未知用户");
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getFriendList(Long userId) {
        List<Long> friendIds = friendMapper.findFriendIds(userId);
        return friendIds.stream().map(friendId -> {
            User friend = userMapper.selectById(friendId);
            Map<String, Object> map = new HashMap<>();
            map.put("userId", friendId);
            map.put("nickname", friend != null ? friend.getNickname() : "未知用户");
            map.put("totalAnalysis", friend != null ? friend.getTotalAnalysis() : 0);
            map.put("legendCount", friend != null ? friend.getLegendCount() : 0);
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeFriend(Long userId, Long friendId) {
        Friend friend1 = friendMapper.findByUserAndFriend(userId, friendId);
        Friend friend2 = friendMapper.findByUserAndFriend(friendId, userId);
        
        if (friend1 != null) {
            friend1.setStatus(0);
            friendMapper.updateById(friend1);
        }
        if (friend2 != null) {
            friend2.setStatus(0);
            friendMapper.updateById(friend2);
        }
    }

    // ==================== 排行榜 ====================
    
    @Override
    public List<Map<String, Object>> getCardRanking(Integer limit) {
        LambdaQueryWrapper<UserCard> wrapper = new LambdaQueryWrapper<>();
        wrapper.select("user_id", "SUM(quantity) as total_cards");
        wrapper.groupBy("user_id");
        wrapper.orderByDesc("total_cards");
        wrapper.last("LIMIT " + limit);
        
        List<Map<String, Object>> rawList = userCardMapper.selectMaps(wrapper);
        return rawList.stream().map(item -> {
            Long userId = Long.valueOf(item.get("user_id").toString());
            User user = userMapper.selectById(userId);
            Map<String, Object> map = new HashMap<>();
            map.put("userId", userId);
            map.put("nickname", user != null ? user.getNickname() : "未知用户");
            map.put("totalCards", item.get("total_cards"));
            map.put("legendCount", user != null ? user.getLegendCount() : 0);
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getAnalysisRanking(Integer limit) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.select("id", "nickname", "total_analysis");
        wrapper.orderByDesc("total_analysis");
        wrapper.last("LIMIT " + limit);
        
        List<User> users = userMapper.selectList(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            Map<String, Object> map = new HashMap<>();
            map.put("rank", i + 1);
            map.put("userId", user.getId());
            map.put("nickname", user.getNickname());
            map.put("totalAnalysis", user.getTotalAnalysis());
            result.add(map);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getAchievementRanking(Integer limit) {
        LambdaQueryWrapper<UserAchievement> wrapper = new LambdaQueryWrapper<>();
        wrapper.select("user_id", "COUNT(*) as achievement_count");
        wrapper.groupBy("user_id");
        wrapper.orderByDesc("achievement_count");
        wrapper.last("LIMIT " + limit);
        
        List<Map<String, Object>> rawList = userAchievementMapper.selectMaps(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < rawList.size(); i++) {
            Map<String, Object> item = rawList.get(i);
            Long userId = Long.valueOf(item.get("user_id").toString());
            User user = userMapper.selectById(userId);
            Map<String, Object> map = new HashMap<>();
            map.put("rank", i + 1);
            map.put("userId", userId);
            map.put("nickname", user != null ? user.getNickname() : "未知用户");
            map.put("achievementCount", item.get("achievement_count"));
            result.add(map);
        }
        return result;
    }

    // ==================== 回复建议 ====================
    
    @Override
    public Map<String, Object> generateReplySuggestion(Long analysisId, Long userId) {
        // 检查是否已有缓存
        LambdaQueryWrapper<ReplySuggestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReplySuggestion::getAnalysisId, analysisId);
        ReplySuggestion existing = replySuggestionMapper.selectOne(wrapper);
        
        if (existing != null) {
            return parseSuggestion(existing);
        }
        
        // 获取分析记录
        AnalysisRecord record = analysisRecordMapper.selectById(analysisId);
        if (record == null) {
            throw new RuntimeException("分析记录不存在");
        }
        
        // 调用AI生成回复建议（复用现有AI能力）
        // 这里简化处理，实际应该调用AI接口
        String suggestions = generateDefaultSuggestions(record);
        
        ReplySuggestion suggestion = new ReplySuggestion();
        suggestion.setAnalysisId(analysisId);
        suggestion.setUserId(userId);
        suggestion.setSuggestions(suggestions);
        replySuggestionMapper.insert(suggestion);
        
        return parseSuggestion(suggestion);
    }

    @Override
    public Map<String, Object> getReplySuggestion(Long analysisId) {
        LambdaQueryWrapper<ReplySuggestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReplySuggestion::getAnalysisId, analysisId);
        ReplySuggestion existing = replySuggestionMapper.selectOne(wrapper);
        
        if (existing == null) {
            return null;
        }
        return parseSuggestion(existing);
    }

    private String generateDefaultSuggestions(AnalysisRecord record) {
        // 简化版本，实际应调用AI接口
        return "{\"suggestions\": [" +
            "{\"tone\": \"温和\", \"content\": \"我理解你的感受，我们可以一起想想办法\"}," +
            "{\"tone\": \"直接\", \"content\": \"我觉得我们可以坦诚地聊聊这个问题\"}," +
            "{\"tone\": \"幽默\", \"content\": \"哈哈，看来我们都需要冷静一下\"}" +
            "]}";
    }

    private Map<String, Object> parseSuggestion(ReplySuggestion suggestion) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", suggestion.getId());
        result.put("analysisId", suggestion.getAnalysisId());
        result.put("suggestions", suggestion.getSuggestions());
        result.put("createdAt", suggestion.getCreatedAt());
        return result;
    }
}
