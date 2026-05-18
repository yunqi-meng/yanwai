package com.yanwai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yanwai.constant.GameConstants;
import com.yanwai.entity.User;
import com.yanwai.mapper.UserMapper;
import com.yanwai.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

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
        userMapper.insert(newUser);

        return newUser;
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
        
        if (user.getMemberLevel() != null && user.getMemberLevel() == 1) {
            return true;
        }

        LocalDate today = LocalDate.now();
        LocalDate lastDate = user.getLastAnalysisDate();

        if (lastDate == null || !lastDate.equals(today)) {
            return true;
        }

        return user.getDailyAnalysisCount() < GameConstants.FREE_DAILY_ANALYSIS;
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
}
