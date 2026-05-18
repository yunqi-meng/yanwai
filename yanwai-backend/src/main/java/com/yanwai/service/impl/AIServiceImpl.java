package com.yanwai.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yanwai.config.AiProperties;
import com.yanwai.constant.AIConstants;
import com.yanwai.dto.AnalysisResultDTO;
import com.yanwai.service.AIService;
import com.yanwai.util.RetryUtil;
import okhttp3.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class AIServiceImpl implements AIService {

    private final OkHttpClient client;
    private final AiProperties aiProperties;
    private final RetryUtil retryUtil;

    public AIServiceImpl(AiProperties aiProperties, RetryUtil retryUtil) {
        this.aiProperties = aiProperties;
        this.retryUtil = retryUtil;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public AnalysisResultDTO analyze(String dialogText) {
        if (aiProperties.getEnableMock()) {
            return MockAiService.generateMockResponse(dialogText);
        }
        try {
            String response = callAIWithRetry(dialogText);
            return parseResponse(response);
        } catch (Exception e) {
            throw new RuntimeException("AI分析失败: " + e.getMessage(), e);
        }
    }

    @Retryable(
            value = {IOException.class, RuntimeException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    private String callAIWithRetry(String dialogText) {
        return callAI(dialogText);
    }

    private String callAI(String dialogText) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", aiProperties.getModel());
            requestBody.put("max_tokens", aiProperties.getMaxTokens());
            requestBody.put("temperature", aiProperties.getTemperature());

            JSONArray messages = new JSONArray();
            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", AIConstants.SYSTEM_PROMPT);
            messages.add(systemMsg);

            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", "请分析以下对话的潜台词：\n" + dialogText);
            messages.add(userMsg);

            requestBody.put("messages", messages);

            Request request = new Request.Builder()
                    .url(aiProperties.getBaseUrl() + "/v1/chat/completions")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + aiProperties.getApiKey())
                    .post(RequestBody.create(requestBody.toJSONString(), MediaType.parse("application/json")))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("AI API调用失败: " + response);
                }
                ResponseBody body = response.body();
                if (body == null) {
                    throw new RuntimeException("AI API返回空响应");
                }
                String responseBody = body.string();
                JSONObject jsonResponse = JSONObject.parseObject(responseBody);
                JSONArray choices = jsonResponse.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    return choices.getJSONObject(0).getJSONObject("message").getString("content");
                }
                throw new RuntimeException("AI API返回格式错误");
            }
        } catch (IOException e) {
            throw new RuntimeException("AI API调用异常", e);
        }
    }

    private AnalysisResultDTO parseResponse(String response) {
        try {
            String jsonStr = extractJson(response);
            JSONObject json = JSONObject.parseObject(jsonStr);
            AnalysisResultDTO result = new AnalysisResultDTO();
            result.setRelationship(json.getString("relationship"));

            if (json.containsKey("emotionCurve")) {
                JSONArray emotionArray = json.getJSONArray("emotionCurve");
                result.setEmotionCurve(parseEmotionCurve(emotionArray));
            }

            if (json.containsKey("translations")) {
                JSONArray transArray = json.getJSONArray("translations");
                result.setTranslations(parseTranslations(transArray));
            }

            if (json.containsKey("advice")) {
                JSONArray adviceArray = json.getJSONArray("advice");
                result.setAdvice(adviceArray.toJavaList(String.class));
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("解析AI响应失败", e);
        }
    }

    private String extractJson(String response) {
        String trimmed = response.trim();
        int startIndex = trimmed.indexOf('{');
        int endIndex = trimmed.lastIndexOf('}');
        if (startIndex >= 0 && endIndex > startIndex) {
            return trimmed.substring(startIndex, endIndex + 1);
        }
        throw new RuntimeException("无法从AI响应中提取JSON");
    }

    private java.util.List<AnalysisResultDTO.EmotionPoint> parseEmotionCurve(JSONArray array) {
        java.util.List<AnalysisResultDTO.EmotionPoint> list = new java.util.ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            JSONObject obj = array.getJSONObject(i);
            AnalysisResultDTO.EmotionPoint point = new AnalysisResultDTO.EmotionPoint();
            point.setIndex(obj.getInteger("index"));
            point.setSpeaker(obj.getString("speaker"));
            point.setEmotionScore(obj.getDouble("emotionScore"));
            point.setEmotionType(obj.getString("emotionType"));
            point.setDescription(obj.getString("description"));
            list.add(point);
        }
        return list;
    }

    private java.util.List<AnalysisResultDTO.TranslationItem> parseTranslations(JSONArray array) {
        java.util.List<AnalysisResultDTO.TranslationItem> list = new java.util.ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            JSONObject obj = array.getJSONObject(i);
            AnalysisResultDTO.TranslationItem item = new AnalysisResultDTO.TranslationItem();
            item.setIndex(obj.getInteger("index"));
            item.setOriginal(obj.getString("original"));
            item.setLiteral(obj.getString("literal"));
            item.setSubtext(obj.getString("subtext"));
            item.setEmotionScore(obj.getDouble("emotionScore"));
            list.add(item);
        }
        return list;
    }
}
