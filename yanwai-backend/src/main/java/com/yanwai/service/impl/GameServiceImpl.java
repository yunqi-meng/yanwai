package com.yanwai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yanwai.constant.GameConstants;
import com.yanwai.dto.AchievementDTO;
import com.yanwai.dto.CardDropResultDTO;
import com.yanwai.dto.PersonalityCardDTO;
import com.yanwai.dto.UserCardDTO;
import com.yanwai.entity.*;
import com.yanwai.mapper.*;
import com.yanwai.service.GameService;
import com.yanwai.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GameServiceImpl implements GameService {

    private final PersonalityCardDefMapper cardDefMapper;
    private final UserCardMapper userCardMapper;
    private final AchievementDefMapper achievementDefMapper;
    private final UserAchievementMapper userAchievementMapper;
    private final UserMapper userMapper;
    private final Double cardDropProbability;

    public GameServiceImpl(
            PersonalityCardDefMapper cardDefMapper,
            UserCardMapper userCardMapper,
            AchievementDefMapper achievementDefMapper,
            UserAchievementMapper userAchievementMapper,
            UserMapper userMapper,
            @Value("${yanwai.game.card-drop-probability:0.3}") Double cardDropProbability) {
        this.cardDefMapper = cardDefMapper;
        this.userCardMapper = userCardMapper;
        this.achievementDefMapper = achievementDefMapper;
        this.userAchievementMapper = userAchievementMapper;
        this.userMapper = userMapper;
        this.cardDropProbability = cardDropProbability;
    }

    @Override
    @Transactional
    public CardDropResultDTO dropCard(Long userId, Integer memberLevel) {
        if (Math.random() > cardDropProbability) {
            return null;
        }

        double[] probabilities = memberLevel != null && memberLevel == 1
                ? GameConstants.RARITY_PROBABILITY_VIP
                : GameConstants.RARITY_PROBABILITY_NORMAL;

        int rarity = selectRarity(probabilities);

        LambdaQueryWrapper<PersonalityCardDef> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PersonalityCardDef::getRarity, rarity);
        List<PersonalityCardDef> cards = cardDefMapper.selectList(wrapper);

        if (cards == null || cards.isEmpty()) {
            return null;
        }

        PersonalityCardDef selectedCard = cards.get((int) (Math.random() * cards.size()));
        return addCard(userId, selectedCard);
    }

    private int selectRarity(double[] probabilities) {
        double random = Math.random();
        double cumulative = 0;
        for (int i = 0; i < probabilities.length; i++) {
            cumulative += probabilities[i];
            if (random <= cumulative) {
                return i + 1;
            }
        }
        return GameConstants.RARITY_NORMAL;
    }

    @Transactional
    public CardDropResultDTO addCard(Long userId, PersonalityCardDef cardDef) {
        LambdaQueryWrapper<UserCard> cardWrapper = new LambdaQueryWrapper<>();
        cardWrapper.eq(UserCard::getUserId, userId)
                   .eq(UserCard::getCardId, cardDef.getId());
        UserCard userCard = userCardMapper.selectOne(cardWrapper);

        CardDropResultDTO result = new CardDropResultDTO();
        result.setCardId(cardDef.getId());
        result.setName(cardDef.getName());
        result.setRarity(cardDef.getRarity());
        result.setEmoji(cardDef.getEmoji());
        result.setDescription(cardDef.getDescription());

        if (userCard == null) {
            UserCard newCard = new UserCard();
            newCard.setUserId(userId);
            newCard.setCardId(cardDef.getId());
            newCard.setQuantity(1);
            newCard.setIsNew(1);
            userCardMapper.insert(newCard);
            result.setIsNew(true);
            
            if (cardDef.getRarity() != null && cardDef.getRarity() >= GameConstants.RARITY_LEGEND) {
                User user = userMapper.selectById(userId);
                if (user != null) {
                    user.setLegendCount(user.getLegendCount() != null ? user.getLegendCount() + 1 : 1);
                    userMapper.updateById(user);
                }
            }
        } else {
            userCard.setQuantity(userCard.getQuantity() + 1);
            userCard.setIsNew(1);
            userCardMapper.updateById(userCard);
            result.setIsNew(false);
        }

        return result;
    }

    @Override
    public List<PersonalityCardDTO> getAllCardDefinitions() {
        List<PersonalityCardDef> cards = cardDefMapper.selectList(null);
        return cards.stream().map(card -> {
            PersonalityCardDTO dto = new PersonalityCardDTO();
            BeanUtils.copyProperties(card, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<UserCardDTO> getUserCards(Long userId) {
        List<PersonalityCardDef> allCards = cardDefMapper.selectList(null);

        LambdaQueryWrapper<UserCard> cardWrapper = new LambdaQueryWrapper<>();
        cardWrapper.eq(UserCard::getUserId, userId);
        List<UserCard> userCards = userCardMapper.selectList(cardWrapper);

        Map<Long, UserCard> userCardMap = userCards.stream()
                .collect(Collectors.toMap(UserCard::getCardId, c -> c));

        List<UserCardDTO> result = new ArrayList<>();
        for (PersonalityCardDef def : allCards) {
            UserCardDTO dto = new UserCardDTO();
            dto.setCardId(def.getId());
            dto.setName(def.getName());
            dto.setRarity(def.getRarity());
            dto.setEmoji(def.getEmoji());
            dto.setDescription(def.getDescription());

            UserCard userCard = userCardMap.get(def.getId());
            if (userCard != null) {
                dto.setQuantity(userCard.getQuantity());
                dto.setIsNew(userCard.getIsNew() == 1);
            } else {
                dto.setQuantity(0);
                dto.setIsNew(false);
            }

            result.add(dto);
        }

        return result;
    }

    @Override
    public List<AchievementDTO> getAllAchievements(Long userId) {
        List<AchievementDef> allAchievements = achievementDefMapper.selectList(null);

        LambdaQueryWrapper<UserAchievement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAchievement::getUserId, userId);
        List<UserAchievement> userAchievements = userAchievementMapper.selectList(wrapper);
        Map<Long, UserAchievement> unlockedMap = userAchievements.stream()
                .collect(Collectors.toMap(UserAchievement::getAchievementId, a -> a));

        return allAchievements.stream().map(def -> {
            AchievementDTO dto = new AchievementDTO();
            BeanUtils.copyProperties(def, dto);
            UserAchievement unlocked = unlockedMap.get(def.getId());
            dto.setUnlocked(unlocked != null);
            dto.setUnlockedAt(unlocked != null ? unlocked.getUnlockedAt() : null);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<AchievementDTO> getUserAchievements(Long userId) {
        LambdaQueryWrapper<UserAchievement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAchievement::getUserId, userId);
        List<UserAchievement> userAchievements = userAchievementMapper.selectList(wrapper);

        List<Long> achievementIds = userAchievements.stream()
                .map(UserAchievement::getAchievementId)
                .collect(Collectors.toList());

        if (achievementIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<AchievementDef> achievements = achievementDefMapper.selectBatchIds(achievementIds);
        Map<Long, UserAchievement> unlockedMap = userAchievements.stream()
                .collect(Collectors.toMap(UserAchievement::getAchievementId, a -> a));

        return achievements.stream().map(def -> {
            AchievementDTO dto = new AchievementDTO();
            BeanUtils.copyProperties(def, dto);
            UserAchievement unlocked = unlockedMap.get(def.getId());
            dto.setUnlocked(true);
            dto.setUnlockedAt(unlocked != null ? unlocked.getUnlockedAt() : null);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<AchievementDTO> checkAndUnlock(Long userId, User userStat) {
        List<AchievementDef> allAchievements = achievementDefMapper.selectList(null);

        LambdaQueryWrapper<UserAchievement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAchievement::getUserId, userId);
        List<UserAchievement> userAchievements = userAchievementMapper.selectList(wrapper);
        Map<Long, UserAchievement> unlockedMap = userAchievements.stream()
                .collect(Collectors.toMap(UserAchievement::getAchievementId, a -> a));

        List<AchievementDTO> newAchievements = new ArrayList<>();

        for (AchievementDef def : allAchievements) {
            if (unlockedMap.containsKey(def.getId())) {
                continue;
            }

            if (checkCondition(def, userStat)) {
                UserAchievement newUnlock = new UserAchievement();
                newUnlock.setUserId(userId);
                newUnlock.setAchievementId(def.getId());
                userAchievementMapper.insert(newUnlock);

                AchievementDTO dto = new AchievementDTO();
                BeanUtils.copyProperties(def, dto);
                dto.setUnlocked(true);
                dto.setUnlockedAt(newUnlock.getUnlockedAt());
                newAchievements.add(dto);
            }
        }

        return newAchievements;
    }

    private boolean checkCondition(AchievementDef def, User user) {
        if (user == null || def.getConditionField() == null) {
            return false;
        }

        int userValue = getFieldValue(user, def.getConditionField());
        int conditionValue = def.getConditionValue();
        String op = def.getConditionOp();

        switch (op) {
            case ">=":
                return userValue >= conditionValue;
            case ">":
                return userValue > conditionValue;
            case "=":
            case "==":
                return userValue == conditionValue;
            case "<":
                return userValue < conditionValue;
            case "<=":
                return userValue <= conditionValue;
            default:
                return false;
        }
    }

    private int getFieldValue(User user, String field) {
        switch (field) {
            case "total_analysis":
                return user.getTotalAnalysis() != null ? user.getTotalAnalysis() : 0;
            case "total_share":
                return user.getTotalShare() != null ? user.getTotalShare() : 0;
            case "late_night_count":
                return user.getLateNightCount() != null ? user.getLateNightCount() : 0;
            case "workplace_count":
                return user.getWorkplaceCount() != null ? user.getWorkplaceCount() : 0;
            case "romance_count":
                return user.getRomanceCount() != null ? user.getRomanceCount() : 0;
            case "login_days":
                return user.getLoginDays() != null ? user.getLoginDays() : 0;
            case "legend_count":
                return user.getLegendCount() != null ? user.getLegendCount() : 0;
            default:
                return 0;
        }
    }
}
