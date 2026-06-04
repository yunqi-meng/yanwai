package com.yanwai.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yanwai.dto.DecodeResponse;
import com.yanwai.dto.Result;
import com.yanwai.entity.AnalysisRecord;
import com.yanwai.entity.User;
import com.yanwai.mapper.AnalysisRecordMapper;
import com.yanwai.mapper.UserMapper;
import com.yanwai.service.AnalysisService;
import com.yanwai.service.UserService;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;
    private final UserService userService;
    private final UserMapper userMapper;
    private final AnalysisRecordMapper analysisRecordMapper;

    public AnalysisController(
            AnalysisService analysisService,
            UserService userService,
            UserMapper userMapper,
            AnalysisRecordMapper analysisRecordMapper) {
        this.analysisService = analysisService;
        this.userService = userService;
        this.userMapper = userMapper;
        this.analysisRecordMapper = analysisRecordMapper;
    }

    @PostMapping("/decode")
    public Result<DecodeResponse> decode(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Map<String, String> request) {
        String text = request.get("text");
        if (text == null || text.trim().isEmpty()) {
            return Result.fail("对话内容不能为空");
        }
        if (text.length() > 5000) {
            return Result.fail("对话内容过长，请限制在5000字以内");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }

        if (!userService.canDoAnalysis(userId)) {
            int limit = (user.getMemberLevel() != null && user.getMemberLevel() == 1) ? 10 : 3;
            String msg = "今日分析次数已用完（" + limit + "/" + limit + "），请明天再来";
            if (user.getMemberLevel() == null || user.getMemberLevel() != 1) {
                msg += "或开通会员";
            }
            return Result.fail(403, msg);
        }

        try {
            DecodeResponse response = analysisService.decode(userId, text);
            return Result.success(response);
        } catch (Exception e) {
            return Result.fail("分析失败: " + e.getMessage());
        }
    }

    @PostMapping("/upload")
    public Result<Map<String, String>> uploadImage(@RequestParam("image") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.fail("图片不能为空");
        }

        try {
            String extractedText = extractTextFromImage(file);
            if (extractedText == null || extractedText.trim().isEmpty()) {
                return Result.fail("未能识别到文字内容，请尝试直接输入文本");
            }
            return Result.success(Map.of("text", extractedText));
        } catch (Exception e) {
            return Result.fail("图片识别失败: " + e.getMessage());
        }
    }

    @PostMapping("/decode-with-image")
    public Result<DecodeResponse> decodeWithImage(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Map<String, Object> request) {
        
        String base64Image = (String) request.get("base64Image");
        String text = (String) request.get("text");
        
        if (base64Image == null || base64Image.trim().isEmpty()) {
            return Result.fail("图片不能为空");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }

        if (!userService.canDoAnalysis(userId)) {
            int limit = (user.getMemberLevel() != null && user.getMemberLevel() == 1) ? 10 : 3;
            String msg = "今日分析次数已用完（" + limit + "/" + limit + "），请明天再来";
            if (user.getMemberLevel() == null || user.getMemberLevel() != 1) {
                msg += "或开通会员";
            }
            return Result.fail(403, msg);
        }

        try {
            DecodeResponse response = analysisService.decodeWithImage(userId, base64Image, text);
            return Result.success(response);
        } catch (Exception e) {
            return Result.fail("图片分析失败: " + e.getMessage());
        }
    }

    @PostMapping("/analyze-image")
    public Result<DecodeResponse> analyzeImage(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam("image") MultipartFile file,
            @RequestParam(value = "text", required = false) String text) {
        if (file == null || file.isEmpty()) {
            return Result.fail("图片不能为空");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }

        if (!userService.canDoAnalysis(userId)) {
            int limit = (user.getMemberLevel() != null && user.getMemberLevel() == 1) ? 10 : 3;
            String msg = "今日分析次数已用完（" + limit + "/" + limit + "），请明天再来";
            if (user.getMemberLevel() == null || user.getMemberLevel() != 1) {
                msg += "或开通会员";
            }
            return Result.fail(403, msg);
        }

        try {
            String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
            DecodeResponse response = analysisService.decodeWithImage(userId, base64Image, text);
            return Result.success(response);
        } catch (Exception e) {
            return Result.fail("图片分析失败: " + e.getMessage());
        }
    }

    private String extractTextFromImage(MultipartFile file) throws IOException {
        String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
        String dataUri = "data:" + file.getContentType() + ";base64," + base64Image;

        JSONObject requestBody = new JSONObject();
        requestBody.put("base64Image", dataUri);
        requestBody.put("language", "chs");

        String ocrApiKey = System.getenv("OCR_API_KEY") != null ? System.getenv("OCR_API_KEY") : "helloworld";
        Request request = new Request.Builder()
                .url("https://api.ocr.space/parse/image")
                .addHeader("apikey", ocrApiKey)
                .post(okhttp3.RequestBody.create(requestBody.toJSONString(), MediaType.parse("application/json")))
                .build();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("OCR API调用失败: " + response);
            }
            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("OCR API返回空响应");
            }

            JSONObject jsonResponse = JSONObject.parseObject(body.string());
            JSONArray parsedResults = jsonResponse.getJSONArray("ParsedResults");
            if (parsedResults == null || parsedResults.isEmpty()) {
                Integer errorCode = jsonResponse.getInteger("ErrorCode");
                if (errorCode != null && errorCode == 407) {
                    throw new IOException("OCR API额度已用尽，请稍后再试或直接输入文本");
                }
                return null;
            }

            StringBuilder text = new StringBuilder();
            for (int i = 0; i < parsedResults.size(); i++) {
                JSONObject result = parsedResults.getJSONObject(i);
                String parsedText = result.getString("ParsedText");
                if (parsedText != null && !parsedText.trim().isEmpty()) {
                    text.append(parsedText.trim());
                    if (i < parsedResults.size() - 1) {
                        text.append("\n");
                    }
                }
            }
            return text.toString();
        }
    }

    @PostMapping("/share")
    public Result<String> share(@RequestHeader("X-User-Id") Long userId) {
        userService.incrementShareCount(userId);
        return Result.success("分享成功");
    }

    @GetMapping("/history")
    public Result<Page<AnalysisRecord>> getHistory(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<AnalysisRecord> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<AnalysisRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnalysisRecord::getUserId, userId);
        wrapper.orderByDesc(AnalysisRecord::getCreatedAt);
        Page<AnalysisRecord> result = analysisRecordMapper.selectPage(pageParam, wrapper);
        result.getRecords().forEach(record -> record.setOriginalImage(null));
        return Result.success(result);
    }

    @DeleteMapping("/history/{id}")
    public Result<String> deleteHistory(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        AnalysisRecord record = analysisRecordMapper.selectById(id);
        if (record == null) {
            return Result.fail("记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            return Result.fail(403, "无权操作此记录");
        }
        analysisRecordMapper.deleteById(id);
        return Result.success("删除成功");
    }

    @GetMapping("/history/{id}")
    public Result<AnalysisRecord> getHistoryDetail(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        AnalysisRecord record = analysisRecordMapper.selectById(id);
        if (record == null) {
            return Result.fail("记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            return Result.fail(403, "无权查看此记录");
        }
        return Result.success(record);
    }
}
