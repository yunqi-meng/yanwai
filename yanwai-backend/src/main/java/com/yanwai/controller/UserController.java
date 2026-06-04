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
        String email = request.get("email");
        String password = request.get("password");
        
        if (email == null || email.isEmpty()) {
            return Result.fail("邮箱不能为空");
        }
        if (password == null || password.isEmpty()) {
            return Result.fail("密码不能为空");
        }
        
        User user = userService.login(email, password);
        if (user == null) {
            return Result.fail("邮箱或密码错误");
        }
        
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

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        
        if (email == null || email.isEmpty()) {
            return Result.fail("邮箱不能为空");
        }
        if (password == null || password.isEmpty()) {
            return Result.fail("密码不能为空");
        }
        
        String validateResult = userService.validateEmail(email);
        if (validateResult != null) {
            return Result.fail(validateResult);
        }
        
        validateResult = userService.validatePassword(password);
        if (validateResult != null) {
            return Result.fail(validateResult);
        }
        
        User user = userService.register(email, password);
        if (user == null) {
            return Result.fail("邮箱已被注册");
        }
        
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

    @PostMapping("/guest-login")
    public Result<Map<String, Object>> guestLogin(@RequestBody Map<String, String> request) {
        String deviceId = request.get("deviceId");
        
        if (deviceId == null || deviceId.isEmpty()) {
            deviceId = "guest_" + System.currentTimeMillis();
        }
        
        User user = userService.loginOrCreateUser(deviceId);
        
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
    
    @PostMapping("/update-info")
    public Result<String> updateUserInfo(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Map<String, String> request) {
        String nickname = request.get("nickname");
        String avatar = request.get("avatar");
        boolean success = userService.updateUserInfo(userId, nickname, avatar);
        if (success) {
            return Result.success("更新成功");
        } else {
            return Result.fail("更新失败");
        }
    }
}
