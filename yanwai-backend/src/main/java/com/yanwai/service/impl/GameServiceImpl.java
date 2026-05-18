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
    private final UserCardFragmentMapper fragmentMapper;
    private final AchievementDefMapper achievementDefMapper;
    private final UserAchievementMapper userAchievementMapper;
    private final Double cardDropProbability;

    public GameServiceImpl(
            PersonalityCardDefMapper cardDefMapper,
            UserCardMapper userCardMapper,
            UserCardFragmentMapper fragmentMapper,
            AchievementDefMapper achievementDefMapper,
            UserAchievementMapper userAchievementMapper,
            @Value("${yanwai.game.card-drop-probability:0.3}") Double cardDropProbability) {
        this.cardDefMapper = cardDefMapper;
        this.userCardMapper = userCardMapper;
        this.fragmentMapper = fragmentMapper;
        this.achievementDefMapper = achievementDefMapper;
        this.userAchievementMapper = userAchievementMapper;
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
        result.setFragmentAdded(0);
        result.setSynthesized(false);

        if (userCard == null) {
            UserCard newCard = new UserCard();
            newCard.setUserId(userId);
            newCard.setCardId(cardDef.getId());
            newCard.setQuantity(1);
            newCard.setIsNew(1);
            userCardMapper.insert(newCard);
            result.setIsNew(true);
        } else {
            int oldQuantity = userCard.getQuantity();
            int newQuantity = oldQuantity + 1;
            userCard.setQuantity(newQuantity);
            userCard.setIsNew(1);
            userCardMapper.updateById(userCard);

            if (newQuantity > 1) {
                int fragments = newQuantity - 1;
                addFragment(userId, cardDef.getId(), fragments);
                result.setFragmentAdded(fragments);
            }
            result.setIsNew(false);
        }

        return result;
    }

    private void addFragment(Long userId, Long cardId, int count) {
        LambdaQueryWrapper<UserCardFragment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCardFragment::getUserId, userId)
               .eq(UserCardFragment::getCardId, cardId);
        UserCardFragment fragment = fragmentMapper.selectOne(wrapper);

        if (fragment == null) {
            fragment = new UserCardFragment();
            fragment.setUserId(userId);
            fragment.setCardId(cardId);
            fragment.setFragmentCount(count);
            fragmentMapper.insert(fragment);
        } else {
            fragment.setFragmentCount(fragment.getFragmentCount() + count);
            fragmentMapper.updateById(fragment);
        }
    }

    @Override
    @Transactional
    public void synthesizeCard(Long userId, Long cardId) {
        LambdaQueryWrapper<UserCardFragment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCardFragment::getUserId, userId)
               .eq(UserCardFragment::getCardId, cardId);
        UserCardFragment fragment = fragmentMapper.selectOne(wrapper);

        if (fragment == null || fragment.getFragmentCount() < GameConstants.FRAGMENT_COUNT_FOR_SYNTHESIS) {
            throw new RuntimeException("碎片数量不足");
        }

        fragment.setFragmentCount(fragment.getFragmentCount() - GameConstants.FRAGMENT_COUNT_FOR_SYNTHESIS);
        if (fragment.getFragmentCount() == 0) {
            fragmentMapper.deleteById(fragment);
        } else {
            fragmentMapper.updateById(fragment);
        }

        LambdaQueryWrapper<UserCard> cardWrapper = new LambdaQueryWrapper<>();
        cardWrapper.eq(UserCard::getUserId, userId)
                   .eq(UserCard::getCardId, cardId);
        UserCard userCard = userCardMapper.selectOne(cardWrapper);

        if (userCard == null) {
            userCard = new UserCard();
            userCard.setUserId(userId);
            userCard.setCardId(cardId);
            userCard.setQuantity(1);
            userCard.setIsNew(1);
            userCardMapper.insert(userCard);
        } else {
            userCard.setQuantity(userCard.getQuantity() + 1);
            userCard.setIsNew(1);
            userCardMapper.updateById(userCard);
        }
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
        Map<Long, PersonalityCardDef> cardDefMap = allCards.stream()
                .collect(Collectors.toMap(PersonalityCardDef::getId, c -> c));

        LambdaQueryWrapper<UserCard> cardWrapper = new LambdaQueryWrapper<>();
        cardWrapper.eq(UserCard::getUserId, userId);
        List<UserCard> userCards = userCardMapper.selectList(cardWrapper);

        LambdaQueryWrapper<UserCardFragment> fragWrapper = new LambdaQueryWrapper<>();
        fragWrapper.eq(UserCardFragment::getUserId, userId);
        List<UserCardFragment> fragments = fragmentMapper.selectList(fragWrapper);
        Map<Long, Integer> fragmentMap = fragments.stream()
                .collect(Collectors.toMap(UserCardFragment::getCardId, UserCardFragment::getFragmentCount));

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

            dto.setFragmentCount(fragmentMap.getOrDefault(def.getId(), 0));
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
            default:
                return 0;
        }
    }
}
