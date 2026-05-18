package com.yanwai.controller;

import com.yanwai.dto.Result;
import com.yanwai.entity.User;
import com.yanwai.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String openid = request.get("openid");
        if (openid == null || openid.isEmpty()) {
            return Result.fail("openid不能为空");
        }
        User user = userService.loginOrCreateUser(openid);
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("nickname", user.getNickname());
        data.put("memberLevel", user.getMemberLevel());
        data.put("dailyAnalysisCount", user.getDailyAnalysisCount());
        data.put("totalAnalysis", user.getTotalAnalysis());
        data.put("totalShare", user.getTotalShare());
        data.put("memberExpireTime", user.getMemberExpireTime() != null ? 
            user.getMemberExpireTime().format(FORMATTER) : null);
        data.put("canWatchAd", userService.canWatchAd(user.getId()));
        return Result.success(data);
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(@RequestHeader(value = "X-User-Id", required = false) String userIdStr) {
        if (userIdStr == null || userIdStr.isEmpty()) {
            return Result.fail("用户不存在");
        }
        
        Long userId;
        try {
            userId = Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            return Result.fail("用户不存在");
        }
        
        User user = userService.getUserById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("nickname", user.getNickname());
        data.put("memberLevel", user.getMemberLevel());
        data.put("dailyAnalysisCount", user.getDailyAnalysisCount());
        data.put("totalAnalysis", user.getTotalAnalysis());
        data.put("totalShare", user.getTotalShare());
        data.put("lateNightCount", user.getLateNightCount());
        data.put("workplaceCount", user.getWorkplaceCount());
        data.put("romanceCount", user.getRomanceCount());
        data.put("memberExpireTime", user.getMemberExpireTime() != null ? 
            user.getMemberExpireTime().format(FORMATTER) : null);
        data.put("canWatchAd", userService.canWatchAd(userId));
        return Result.success(data);
    }

    @PostMapping("/reset")
    public Result<String> reset(@RequestHeader("X-User-Id") Long userId) {
        userService.resetUserData(userId);
        return Result.success("数据重置成功");
    }
    
    @GetMapping("/can-watch-ad")
    public Result<Map<String, Object>> canWatchAd(@RequestHeader("X-User-Id") Long userId) {
        boolean canWatch = userService.canWatchAd(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("canWatch", canWatch);
        return Result.success(data);
    }
    
    @PostMapping("/watch-ad-complete")
    public Result<String> watchAdComplete(@RequestHeader("X-User-Id") Long userId) {
        boolean success = userService.watchAd(userId);
        if (success) {
            return Result.success("会员开通成功");
        } else {
            return Result.fail("今日已观看广告或会员尚未过期");
        }
    }
}
