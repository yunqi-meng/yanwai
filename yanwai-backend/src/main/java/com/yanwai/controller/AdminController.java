package com.yanwai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yanwai.dto.Result;
import com.yanwai.entity.*;
import com.yanwai.service.AdminService;
import com.yanwai.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final JwtUtil jwtUtil;

    public AdminController(AdminService adminService, JwtUtil jwtUtil) {
        this.adminService = adminService;
        this.jwtUtil = jwtUtil;
    }

    // ==================== 管员认证 ====================
    
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        
        if (username == null || username.isEmpty()) {
            return Result.fail("用户名不能为空");
        }
        if (password == null || password.isEmpty()) {
            return Result.fail("密码不能为空");
        }
        
        Admin admin = adminService.login(username, password);
        if (admin == null) {
            return Result.fail("用户名或密码错误");
        }
        
        String token = jwtUtil.generateToken(admin.getId(), admin.getUsername(), admin.getRole());
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("adminId", admin.getId());
        data.put("username", admin.getUsername());
        data.put("realName", admin.getRealName());
        data.put("role", admin.getRole());
        return Result.success(data);
    }

    @GetMapping("/info")
    public Result<Admin> getAdminInfo(@RequestHeader(value = "X-Admin-Id", required = false) Long adminId) {
        if (adminId == null) {
            return Result.fail("未登录");
        }
        Admin admin = adminService.getAdminById(adminId);
        if (admin == null) {
            return Result.fail("管理员不存在");
        }
        return Result.success(admin);
    }

    // ==================== 数据看板 ====================
    
    @GetMapping("/dashboard/overview")
    public Result<Map<String, Object>> getDashboardOverview() {
        return Result.success(adminService.getDashboardOverview());
    }

    @GetMapping("/dashboard/trend")
    public Result<Map<String, Object>> getTrend(@RequestParam(defaultValue = "7") int days) {
        Map<String, Object> data = new HashMap<>();
        data.put("userTrend", adminService.getUserTrend(days));
        data.put("analysisTrend", adminService.getAnalysisTrend(days));
        return Result.success(data);
    }

    @GetMapping("/dashboard/hot")
    public Result<List<Map<String, Object>>> getHotAnalysis(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(adminService.getHotAnalysis(limit));
    }

    // ==================== 用户管理 ====================
    
    @GetMapping("/users")
    public Result<Page<User>> getUserList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer memberLevel) {
        return Result.success(adminService.getUserList(page, size, keyword, memberLevel));
    }

    @GetMapping("/users/{id}")
    public Result<User> getUserDetail(@PathVariable Long id) {
        User user = adminService.getUserDetail(id);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        return Result.success(user);
    }

    @PutMapping("/users/{id}")
    public Result<String> updateUser(@PathVariable Long id, @RequestBody User user,
                                     @RequestHeader(value = "X-Admin-Id", required = false) Long adminId,
                                     HttpServletRequest request) {
        adminService.updateUser(id, user);
        saveLog(adminId, "更新用户", "PUT", "id=" + id, request);
        return Result.success("更新成功");
    }

    @PostMapping("/users/{id}/ban")
    public Result<String> banUser(@PathVariable Long id,
                                  @RequestHeader(value = "X-Admin-Id", required = false) Long adminId,
                                  HttpServletRequest request) {
        adminService.banUser(id);
        saveLog(adminId, "封禁用户", "POST", "id=" + id, request);
        return Result.success("封禁成功");
    }

    @PostMapping("/users/{id}/unban")
    public Result<String> unbanUser(@PathVariable Long id,
                                    @RequestHeader(value = "X-Admin-Id", required = false) Long adminId,
                                    HttpServletRequest request) {
        adminService.unbanUser(id);
        saveLog(adminId, "解封用户", "POST", "id=" + id, request);
        return Result.success("解封成功");
    }

    @PostMapping("/users/{id}/member")
    public Result<String> updateMemberLevel(@PathVariable Long id,
                                            @RequestBody Map<String, Object> request,
                                            @RequestHeader(value = "X-Admin-Id", required = false) Long adminId,
                                            HttpServletRequest httpRequest) {
        Integer level = (Integer) request.get("level");
        Integer days = (Integer) request.get("days");
        adminService.updateMemberLevel(id, level, days);
        saveLog(adminId, "更新会员", "POST", "id=" + id + ",level=" + level, httpRequest);
        return Result.success("会员更新成功");
    }

    // ==================== 卡牌管理 ====================
    
    @GetMapping("/cards")
    public Result<Page<PersonalityCardDef>> getCardList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer rarity) {
        return Result.success(adminService.getCardList(page, size, rarity));
    }

    @GetMapping("/cards/{id}")
    public Result<PersonalityCardDef> getCardById(@PathVariable Long id) {
        PersonalityCardDef card = adminService.getCardById(id);
        if (card == null) {
            return Result.fail("卡牌不存在");
        }
        return Result.success(card);
    }

    @PostMapping("/cards")
    public Result<String> createCard(@RequestBody PersonalityCardDef card,
                                     @RequestHeader(value = "X-Admin-Id", required = false) Long adminId,
                                     HttpServletRequest request) {
        adminService.createCard(card);
        saveLog(adminId, "创建卡牌", "POST", "name=" + card.getName(), request);
        return Result.success("创建成功");
    }

    @PutMapping("/cards/{id}")
    public Result<String> updateCard(@PathVariable Long id, @RequestBody PersonalityCardDef card,
                                     @RequestHeader(value = "X-Admin-Id", required = false) Long adminId,
                                     HttpServletRequest request) {
        adminService.updateCard(id, card);
        saveLog(adminId, "更新卡牌", "PUT", "id=" + id, request);
        return Result.success("更新成功");
    }

    @DeleteMapping("/cards/{id}")
    public Result<String> deleteCard(@PathVariable Long id,
                                     @RequestHeader(value = "X-Admin-Id", required = false) Long adminId,
                                     HttpServletRequest request) {
        adminService.deleteCard(id);
        saveLog(adminId, "删除卡牌", "DELETE", "id=" + id, request);
        return Result.success("删除成功");
    }

    // ==================== 成就管理 ====================
    
    @GetMapping("/achievements")
    public Result<Page<AchievementDef>> getAchievementList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(adminService.getAchievementList(page, size));
    }

    @GetMapping("/achievements/{id}")
    public Result<AchievementDef> getAchievementById(@PathVariable Long id) {
        AchievementDef achievement = adminService.getAchievementById(id);
        if (achievement == null) {
            return Result.fail("成就不存在");
        }
        return Result.success(achievement);
    }

    @PostMapping("/achievements")
    public Result<String> createAchievement(@RequestBody AchievementDef achievement,
                                            @RequestHeader(value = "X-Admin-Id", required = false) Long adminId,
                                            HttpServletRequest request) {
        adminService.createAchievement(achievement);
        saveLog(adminId, "创建成就", "POST", "name=" + achievement.getName(), request);
        return Result.success("创建成功");
    }

    @PutMapping("/achievements/{id}")
    public Result<String> updateAchievement(@PathVariable Long id, @RequestBody AchievementDef achievement,
                                            @RequestHeader(value = "X-Admin-Id", required = false) Long adminId,
                                            HttpServletRequest request) {
        adminService.updateAchievement(id, achievement);
        saveLog(adminId, "更新成就", "PUT", "id=" + id, request);
        return Result.success("更新成功");
    }

    @DeleteMapping("/achievements/{id}")
    public Result<String> deleteAchievement(@PathVariable Long id,
                                            @RequestHeader(value = "X-Admin-Id", required = false) Long adminId,
                                            HttpServletRequest request) {
        adminService.deleteAchievement(id);
        saveLog(adminId, "删除成就", "DELETE", "id=" + id, request);
        return Result.success("删除成功");
    }

    // ==================== 分析记录管理 ====================
    
    @GetMapping("/analysis")
    public Result<Page<AnalysisRecord>> getAnalysisList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(adminService.getAnalysisList(page, size, userId, startDate, endDate));
    }

    @GetMapping("/analysis/{id}")
    public Result<AnalysisRecord> getAnalysisById(@PathVariable Long id) {
        AnalysisRecord record = adminService.getAnalysisById(id);
        if (record == null) {
            return Result.fail("记录不存在");
        }
        return Result.success(record);
    }

    @DeleteMapping("/analysis/{id}")
    public Result<String> deleteAnalysis(@PathVariable Long id,
                                         @RequestHeader(value = "X-Admin-Id", required = false) Long adminId,
                                         HttpServletRequest request) {
        adminService.deleteAnalysis(id);
        saveLog(adminId, "删除分析记录", "DELETE", "id=" + id, request);
        return Result.success("删除成功");
    }

    // ==================== 系统配置 ====================
    
    @GetMapping("/config")
    public Result<List<SystemConfig>> getAllConfigs() {
        return Result.success(adminService.getAllConfigs());
    }

    @PutMapping("/config")
    public Result<String> updateConfig(@RequestBody Map<String, String> request,
                                       @RequestHeader(value = "X-Admin-Id", required = false) Long adminId,
                                       HttpServletRequest httpRequest) {
        String key = request.get("key");
        String value = request.get("value");
        if (key == null || key.isEmpty()) {
            return Result.fail("配置键不能为空");
        }
        adminService.updateConfig(key, value);
        saveLog(adminId, "更新配置", "PUT", "key=" + key, httpRequest);
        return Result.success("更新成功");
    }

    // ==================== 操作日志 ====================
    
    @GetMapping("/logs")
    public Result<Page<OperationLog>> getLogList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long adminId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(adminService.getLogList(page, size, adminId, startDate, endDate));
    }

    // ==================== 辅助方法 ====================
    
    private void saveLog(Long adminId, String operation, String method, String params, HttpServletRequest request) {
        if (adminId == null) return;
        OperationLog log = new OperationLog();
        log.setAdminId(adminId);
        log.setOperation(operation);
        log.setMethod(method);
        log.setParams(params);
        log.setIp(getClientIp(request));
        adminService.saveLog(log);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null ? ip.split(",")[0].trim() : "unknown";
    }
}
