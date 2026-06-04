package com.yanwai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yanwai.constant.GameConstants;
import com.yanwai.entity.User;
import com.yanwai.mapper.UserMapper;
import com.yanwai.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{6,}$");

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public User loginOrCreateUser(String openid) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        User existingUser = userMapper.selectOne(wrapper);

        if (existingUser != null) {
            checkAndUpdateMemberStatus(existingUser);
            updateLoginDays(existingUser);
            return existingUser;
        }

        User newUser = new User();
        newUser.setOpenid(openid);
        newUser.setNickname("用户" + System.currentTimeMillis() % 10000);
        newUser.setMemberLevel(0);
        newUser.setDailyAnalysisCount(0);
        newUser.setTotalAnalysis(0);
        newUser.setTotalShare(0);
        newUser.setLateNightCount(0);
        newUser.setWorkplaceCount(0);
        newUser.setRomanceCount(0);
        newUser.setMemberExpireTime(null);
        newUser.setLastAdWatchDate(null);
        newUser.setLoginDays(1);
        newUser.setLegendCount(0);
        newUser.setLastLoginDate(LocalDate.now());
        userMapper.insert(newUser);

        return newUser;
    }

    @Override
    public User login(String email, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        User user = userMapper.selectOne(wrapper);
        
        if (user != null && verifyPassword(password, user.getPassword())) {
            checkAndUpdateMemberStatus(user);
            updateLoginDays(user);
            return user;
        }
        
        return null;
    }

    @Override
    @Transactional
    public User register(String email, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        if (userMapper.selectOne(wrapper) != null) {
            return null;
        }
        
        User newUser = new User();
        newUser.setOpenid(UUID.randomUUID().toString().replace("-", ""));
        newUser.setEmail(email);
        newUser.setPassword(encryptPassword(password));
        newUser.setNickname("用户" + System.currentTimeMillis() % 10000);
        newUser.setMemberLevel(0);
        newUser.setDailyAnalysisCount(0);
        newUser.setTotalAnalysis(0);
        newUser.setTotalShare(0);
        newUser.setLateNightCount(0);
        newUser.setWorkplaceCount(0);
        newUser.setRomanceCount(0);
        newUser.setMemberExpireTime(null);
        newUser.setLastAdWatchDate(null);
        newUser.setLoginDays(1);
        newUser.setLegendCount(0);
        newUser.setLastLoginDate(LocalDate.now());
        userMapper.insert(newUser);
        
        return newUser;
    }

    @Override
    public String validateEmail(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return "请输入有效的邮箱地址";
        }
        return null;
    }

    @Override
    public String validatePassword(String password) {
        if (password.length() < 6) {
            return "密码长度至少6位";
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            return "密码需包含字母和数字";
        }
        return null;
    }

    private String encryptPassword(String password) {
        return DigestUtils.md5DigestAsHex(password.getBytes());
    }

    private boolean verifyPassword(String inputPassword, String storedPassword) {
        return encryptPassword(inputPassword).equals(storedPassword);
    }

    private void updateLoginDays(User user) {
        LocalDate today = LocalDate.now();
        LocalDate lastLogin = user.getLastLoginDate();
        
        if (lastLogin == null) {
            user.setLoginDays(1);
        } else if (lastLogin.equals(today)) {
            return;
        } else if (lastLogin.plusDays(1).equals(today)) {
            user.setLoginDays(user.getLoginDays() != null ? user.getLoginDays() + 1 : 1);
        } else {
            user.setLoginDays(1);
        }
        
        user.setLastLoginDate(today);
        userMapper.updateById(user);
    }

    private void checkAndUpdateMemberStatus(User user) {
        LocalDateTime now = LocalDateTime.now();
        if (user.getMemberLevel() == 1 && 
            user.getMemberExpireTime() != null && 
            now.isAfter(user.getMemberExpireTime())) {
            user.setMemberLevel(0);
            userMapper.updateById(user);
        }
    }

    @Override
    public User getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            checkAndUpdateMemberStatus(user);
        }
        return user;
    }

    @Override
    @Transactional
    public void resetUserData(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setDailyAnalysisCount(0);
            user.setLastAnalysisDate(null);
            user.setTotalAnalysis(0);
            user.setTotalShare(0);
            user.setLateNightCount(0);
            user.setWorkplaceCount(0);
            userMapper.updateById(user);
        }
    }

    @Override
    @Transactional
    public void incrementShareCount(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setTotalShare(user.getTotalShare() + 1);
            userMapper.updateById(user);
        }
    }

    @Override
    @Transactional
    public void updateDailyAnalysisCount(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
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

    @Override
    public boolean canDoAnalysis(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }

        checkAndUpdateMemberStatus(user);
        user = userMapper.selectById(userId);
        
        LocalDate today = LocalDate.now();
        LocalDate lastDate = user.getLastAnalysisDate();

        if (lastDate == null || !lastDate.equals(today)) {
            return true;
        }

        int dailyLimit = (user.getMemberLevel() != null && user.getMemberLevel() == 1) 
                ? GameConstants.VIP_DAILY_ANALYSIS 
                : GameConstants.FREE_DAILY_ANALYSIS;

        return user.getDailyAnalysisCount() < dailyLimit;
    }

    @Override
    @Transactional
    public boolean watchAd(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || !canWatchAd(userId)) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        user.setMemberLevel(1);
        user.setMemberExpireTime(now.toLocalDate().plusDays(1).atStartOfDay());
        user.setLastAdWatchDate(LocalDate.now());
        userMapper.updateById(user);
        
        return true;
    }

    @Override
    public boolean canWatchAd(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        
        LocalDate today = LocalDate.now();
        LocalDate lastWatchDate = user.getLastAdWatchDate();
        
        if (lastWatchDate != null && lastWatchDate.equals(today)) {
            return false;
        }
        
        return true;
    }
    
    @Override
    @Transactional
    public boolean updateUserInfo(Long userId, String nickname, String avatar) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        
        if (nickname != null && !nickname.isEmpty()) {
            if (nickname.length() > 20) {
                nickname = nickname.substring(0, 20);
            }
            
            int count = userMapper.countByNicknameExcludeId(nickname, userId);
            if (count > 0) {
                return false;
            }
            
            user.setNickname(nickname);
        }
        
        userMapper.updateById(user);
        return true;
    }
}
