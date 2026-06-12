package com.yanwai.controller;

import com.yanwai.dto.Result;
import com.yanwai.service.SocialService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/social")
public class SocialController {

    private final SocialService socialService;

    public SocialController(SocialService socialService) {
        this.socialService = socialService;
    }

    // ==================== 签到系统 ====================

    @PostMapping("/checkin")
    public Result<Map<String, Object>> checkin(@RequestHeader("X-User-Id") Long userId) {
        try {
            Map<String, Object> result = socialService.checkin(userId);
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    @GetMapping("/checkin/status")
    public Result<Map<String, Object>> getCheckinStatus(@RequestHeader("X-User-Id") Long userId) {
        Map<String, Object> status = socialService.getCheckinStatus(userId);
        return Result.success(status);
    }

    @GetMapping("/checkin/ranking")
    public Result<List<Map<String, Object>>> getCheckinRanking(
            @RequestParam(defaultValue = "20") Integer limit) {
        return Result.success(socialService.getCheckinRanking(limit));
    }

    // ==================== 好友系统 ====================

    @PostMapping("/friend/request")
    public Result<String> sendFriendRequest(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Map<String, Object> request) {
        try {
            Long toUserId = Long.valueOf(request.get("toUserId").toString());
            String message = (String) request.getOrDefault("message", "");
            socialService.sendFriendRequest(userId, toUserId, message);
            return Result.success("好友申请已发送");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/friend/request/{requestId}/handle")
    public Result<String> handleFriendRequest(
            @PathVariable Long requestId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam boolean accept) {
        try {
            socialService.handleFriendRequest(requestId, userId, accept);
            return Result.success(accept ? "已同意好友申请" : "已拒绝好友申请");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    @GetMapping("/friend/requests")
    public Result<List<Map<String, Object>>> getPendingRequests(
            @RequestHeader("X-User-Id") Long userId) {
        return Result.success(socialService.getPendingRequests(userId));
    }

    @GetMapping("/friend/list")
    public Result<List<Map<String, Object>>> getFriendList(
            @RequestHeader("X-User-Id") Long userId) {
        return Result.success(socialService.getFriendList(userId));
    }

    @DeleteMapping("/friend/{friendId}")
    public Result<String> removeFriend(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long friendId) {
        socialService.removeFriend(userId, friendId);
        return Result.success("已删除好友");
    }

    // ==================== 排行榜 ====================

    @GetMapping("/ranking/cards")
    public Result<List<Map<String, Object>>> getCardRanking(
            @RequestParam(defaultValue = "20") Integer limit) {
        return Result.success(socialService.getCardRanking(limit));
    }

    @GetMapping("/ranking/analysis")
    public Result<List<Map<String, Object>>> getAnalysisRanking(
            @RequestParam(defaultValue = "20") Integer limit) {
        return Result.success(socialService.getAnalysisRanking(limit));
    }

    @GetMapping("/ranking/achievements")
    public Result<List<Map<String, Object>>> getAchievementRanking(
            @RequestParam(defaultValue = "20") Integer limit) {
        return Result.success(socialService.getAchievementRanking(limit));
    }

    // ==================== 回复建议 ====================

    @PostMapping("/reply-suggestion/{analysisId}")
    public Result<Map<String, Object>> generateReplySuggestion(
            @PathVariable Long analysisId,
            @RequestHeader("X-User-Id") Long userId) {
        try {
            Map<String, Object> suggestion = socialService.generateReplySuggestion(analysisId, userId);
            return Result.success(suggestion);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    @GetMapping("/reply-suggestion/{analysisId}")
    public Result<Map<String, Object>> getReplySuggestion(@PathVariable Long analysisId) {
        Map<String, Object> suggestion = socialService.getReplySuggestion(analysisId);
        if (suggestion == null) {
            return Result.fail("暂无回复建议");
        }
        return Result.success(suggestion);
    }
}
