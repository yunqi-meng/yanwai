package com.yanwai.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yanwai.config.AiProperties;
import com.yanwai.constant.AIConstants;
import com.yanwai.dto.AnalysisResultDTO;
import com.yanwai.service.AIService;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Service
public class AIServiceImpl implements AIService {

    private static final Logger log = LoggerFactory.getLogger(AIServiceImpl.class);

    private final OkHttpClient client;
    private final AiProperties aiProperties;

    public AIServiceImpl(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
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
            String response = callAI(dialogText);
            return parseResponse(response);
        } catch (Exception e) {
            throw new RuntimeException("AI分析失败: " + e.getMessage(), e);
        }
    }

    @Override
    public AnalysisResultDTO analyzeWithImage(String base64Image, String dialogText) {
        if (aiProperties.getEnableMock()) {
            return MockAiService.generateMockResponse(dialogText);
        }
        try {
            String response = callAIWithImage(base64Image, dialogText);
            return parseResponse(response);
        } catch (Exception e) {
            throw new RuntimeException("AI图片分析失败: " + e.getMessage(), e);
        }
    }

    private String compressImage(String base64Image) {
        try {
            String imageData = base64Image;
            if (imageData.contains(",")) {
                imageData = imageData.split(",")[1];
            }
            
            byte[] imageBytes = Base64.getDecoder().decode(imageData);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            BufferedImage originalImage = ImageIO.read(bis);
            
            if (originalImage == null) {
                return base64Image;
            }
            
            int maxWidth = 800;
            int maxHeight = 800;
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            
            if (width <= maxWidth && height <= maxHeight) {
                return base64Image;
            }
            
            double ratio = Math.min((double) maxWidth / width, (double) maxHeight / height);
            int newWidth = (int) (width * ratio);
            int newHeight = (int) (height * ratio);
            
            BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resizedImage.createGraphics();
            g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
            g.dispose();
            
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "jpg", bos);
            byte[] compressedBytes = bos.toByteArray();
            
            log.info("图片压缩完成: {}KB -> {}KB", imageBytes.length / 1024, compressedBytes.length / 1024);
            return Base64.getEncoder().encodeToString(compressedBytes);
            
        } catch (Exception e) {
            log.warn("图片压缩失败，使用原图: {}", e.getMessage());
            return base64Image;
        }
    }

    private String callAIWithImage(String base64Image, String dialogText) {
        try {
            String compressedImage = compressImage(base64Image);
            
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", aiProperties.getVisionModel());

            JSONArray inputArray = new JSONArray();
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");

            JSONArray contentArray = new JSONArray();
            JSONObject imageContent = new JSONObject();
            imageContent.put("type", "input_image");
            imageContent.put("image_url", "data:image/jpeg;base64," + compressedImage);
            contentArray.add(imageContent);

            JSONObject textContent = new JSONObject();
            textContent.put("type", "input_text");
            textContent.put("text", "分析聊天截图：关系、情绪曲线、潜台词、建议。返回JSON：{relationship:\"\",emotionCurve:[{index:0,speaker:\"A\",emotionScore:0.5,emotionType:\"neutral\",description:\"\"}],translations:[{index:0,original:\"\",literal:\"\",subtext:\"\",emotionScore:0.5}],advice:[\"\",\"\",\"\"]}。emotionScore:0-1，emotionType:positive/negative/neutral/anxious/happy/sad/angry/fearful，relationship:情侣/夫妻/暧昧/朋友/同事/上下级/陌生人/家人。");
            contentArray.add(textContent);

            userMsg.put("content", contentArray);
            inputArray.add(userMsg);

            requestBody.put("input", inputArray);

            String visionUrl = aiProperties.getVisionUrl();
            log.info("Vision API URL: {}", visionUrl);
            log.info("Vision API Key: {}", aiProperties.getVisionApiKey() != null ? "已设置" : "未设置");
            log.info("Vision Model: {}", aiProperties.getVisionModel());
            log.info("Request body size: {} bytes", requestBody.toJSONString().length());

            Request request = new Request.Builder()
                    .url(visionUrl)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + aiProperties.getVisionApiKey())
                    .post(RequestBody.create(requestBody.toJSONString(), MediaType.parse("application/json")))
                    .build();

            log.info("发送Vision API请求...");

            try (Response response = client.newCall(request).execute()) {
                log.info("Vision API响应状态: {}", response.code());
                
                if (!response.isSuccessful()) {
                    ResponseBody errorBody = response.body();
                    String errorMsg = errorBody != null ? errorBody.string() : "Unknown error";
                    log.error("Vision API调用失败 (code={}): {}", response.code(), errorMsg);
                    throw new RuntimeException("Vision API调用失败 (code=" + response.code() + "): " + errorMsg);
                }
                ResponseBody body = response.body();
                if (body == null) {
                    throw new RuntimeException("Vision API返回空响应");
                }
                String responseBody = body.string();
                log.info("Vision API响应长度: {} bytes", responseBody.length());
                
                JSONObject jsonResponse = JSONObject.parseObject(responseBody);
                
                JSONArray outputs = jsonResponse.getJSONArray("output");
                if (outputs != null && !outputs.isEmpty()) {
                    for (int i = 0; i < outputs.size(); i++) {
                        JSONObject output = outputs.getJSONObject(i);
                        String type = output.getString("type");
                        if ("message".equals(type)) {
                            JSONArray content = output.getJSONArray("content");
                            if (content != null && !content.isEmpty()) {
                                for (int j = 0; j < content.size(); j++) {
                                    JSONObject item = content.getJSONObject(j);
                                    if ("output_text".equals(item.getString("type"))) {
                                        String text = item.getString("text");
                                        log.info("获取到Vision分析结果，长度: {} chars", text.length());
                                        return text;
                                    }
                                }
                            }
                        }
                    }
                }
                log.warn("Vision API响应格式不符合预期，返回原始响应");
                return jsonResponse.toJSONString();
            }
        } catch (IOException e) {
            log.error("Vision API调用IO异常: {}", e.getMessage(), e);
            throw new RuntimeException("Vision API调用异常: " + e.getMessage(), e);
        }
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
