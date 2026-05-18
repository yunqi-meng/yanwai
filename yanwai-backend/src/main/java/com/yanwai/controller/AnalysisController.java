package com.yanwai.controller;

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
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }

        if (user.getMemberLevel() == null || user.getMemberLevel() != 1) {
            if (!userService.canDoAnalysis(userId)) {
                return Result.fail(403, "今日免费分析次数已用完，请明天再来或开通会员");
            }
        }

        try {
            DecodeResponse response = analysisService.decode(userId, text);
            return Result.success(response);
        } catch (Exception e) {
            return Result.fail("分析失败: " + e.getMessage());
        }
    }

    @PostMapping("/share")
    public Result<String> share(@RequestHeader("X-User-Id") Long userId) {
        userService.incrementShareCount(userId);
        User user = userMapper.selectById(userId);
        if (user != null) {
            userService.updateDailyAnalysisCount(userId);
        }
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
