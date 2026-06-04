package com.yanwai.service;

import com.yanwai.dto.AchievementDTO;
import com.yanwai.dto.CardDropResultDTO;
import com.yanwai.dto.PersonalityCardDTO;
import com.yanwai.dto.UserCardDTO;
import com.yanwai.entity.User;
import java.util.List;

public interface GameService {
    CardDropResultDTO dropCard(Long userId, Integer memberLevel);

    List<AchievementDTO> checkAndUnlock(Long userId, User userStat);

    List<PersonalityCardDTO> getAllCardDefinitions();

    List<UserCardDTO> getUserCards(Long userId);

    List<AchievementDTO> getAllAchievements(Long userId);

    List<AchievementDTO> getUserAchievements(Long userId);
}
