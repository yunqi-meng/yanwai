package com.yanwai.service.impl;

import com.alibaba.fastjson.JSON;
import com.yanwai.dto.*;
import com.yanwai.entity.AnalysisRecord;
import com.yanwai.entity.User;
import com.yanwai.mapper.AnalysisRecordMapper;
import com.yanwai.mapper.UserMapper;
import com.yanwai.service.AIService;
import com.yanwai.service.AnalysisService;
import com.yanwai.service.GameService;
import com.yanwai.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class AnalysisServiceImpl implements AnalysisService {

    private final AIService aiService;
    private final GameService gameService;
    private final UserService userService;
    private final AnalysisRecordMapper analysisRecordMapper;
    private final UserMapper userMapper;

    public AnalysisServiceImpl(
            AIService aiService,
            GameService gameService,
            UserService userService,
            AnalysisRecordMapper analysisRecordMapper,
            UserMapper userMapper) {
        this.aiService = aiService;
        this.gameService = gameService;
        this.userService = userService;
        this.analysisRecordMapper = analysisRecordMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public DecodeResponse decode(Long userId, String text) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        AnalysisResultDTO analysisResult = aiService.analyze(text);

        AnalysisRecord record = new AnalysisRecord();
        record.setUserId(userId);
        record.setOriginalText(text);
        record.setRelationship(analysisResult.getRelationship());
        record.setAnalysisResult(JSON.toJSONString(analysisResult));
        record.setEmotionCurve(JSON.toJSONString(analysisResult.getEmotionCurve()));
        record.setTranslations(JSON.toJSONString(analysisResult.getTranslations()));
        record.setAdvice(JSON.toJSONString(analysisResult.getAdvice()));
        analysisRecordMapper.insert(record);

        updateUserStats(user, text);

        CardDropResultDTO cardDrop = gameService.dropCard(userId, user.getMemberLevel());

        User updatedUser = userMapper.selectById(userId);
        List<AchievementDTO> newAchievements = gameService.checkAndUnlock(userId, updatedUser);

        DecodeResponse response = new DecodeResponse();
        response.setAnalysis(analysisResult);
        response.setNewCard(cardDrop);
        response.setNewAchievements(newAchievements);

        return response;
    }

    @Override
    @Transactional
    public DecodeResponse decodeWithImage(Long userId, String base64Image, String dialogText) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        AnalysisResultDTO analysisResult = aiService.analyzeWithImage(base64Image, dialogText);

        String originalText = dialogText != null ? dialogText : "[来自图片的分析]";

        AnalysisRecord record = new AnalysisRecord();
        record.setUserId(userId);
        record.setOriginalText(originalText);
        record.setRelationship(analysisResult.getRelationship());
        record.setAnalysisResult(JSON.toJSONString(analysisResult));
        record.setEmotionCurve(JSON.toJSONString(analysisResult.getEmotionCurve()));
        record.setTranslations(JSON.toJSONString(analysisResult.getTranslations()));
        record.setAdvice(JSON.toJSONString(analysisResult.getAdvice()));
        record.setOriginalImage(base64Image);
        analysisRecordMapper.insert(record);

        updateUserStats(user, originalText);

        CardDropResultDTO cardDrop = gameService.dropCard(userId, user.getMemberLevel());

        User updatedUser = userMapper.selectById(userId);
        List<AchievementDTO> newAchievements = gameService.checkAndUnlock(userId, updatedUser);

        DecodeResponse response = new DecodeResponse();
        response.setAnalysis(analysisResult);
        response.setNewCard(cardDrop);
        response.setNewAchievements(newAchievements);
        response.setOriginalImage(base64Image);

        return response;
    }

    private void updateUserStats(User user, String text) {
        LocalTime now = LocalTime.now();
        if (now.isAfter(LocalTime.of(22, 0)) || now.isBefore(LocalTime.of(6, 0))) {
            user.setLateNightCount(user.getLateNightCount() + 1);
        }

        String lowerText = text.toLowerCase();
        if (lowerText.contains("工作") || lowerText.contains("老板") || 
            lowerText.contains("同事") || lowerText.contains("开会") ||
            lowerText.contains("上班") || lowerText.contains("下班")) {
            user.setWorkplaceCount(user.getWorkplaceCount() + 1);
        }

        if (lowerText.contains("喜欢") || lowerText.contains("爱") || 
            lowerText.contains("分手") || lowerText.contains("约会") ||
            lowerText.contains("恋爱") || lowerText.contains("女朋友") ||
            lowerText.contains("男朋友")) {
            user.setRomanceCount(user.getRomanceCount() + 1);
        }

        LocalDate today = LocalDate.now();
        LocalDate lastDate = user.getLastAnalysisDate();
        if (lastDate == null || !lastDate.equals(today)) {
            user.setDailyAnalysisCount(1);
            user.setLastAnalysisDate(today);
        } else {
            user.setDailyAnalysisCount(user.getDailyAnalysisCount() + 1);
        }
        user.setTotalAnalysis(user.getTotalAnalysis() + 1);

        userMapper.updateById(user);
    }
}
