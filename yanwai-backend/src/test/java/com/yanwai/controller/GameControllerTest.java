package com.yanwai.controller;

import com.yanwai.dto.AchievementDTO;
import com.yanwai.dto.PersonalityCardDTO;
import com.yanwai.dto.UserCardDTO;
import com.yanwai.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GameController.class)
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameService gameService;

    private List<AchievementDTO> achievementList;
    private List<PersonalityCardDTO> cardList;
    private List<UserCardDTO> userCardList;

    @BeforeEach
    void setUp() {
        AchievementDTO achievement = new AchievementDTO();
        achievement.setId(1L);
        achievement.setName("首次分析");
        achievement.setDescription("完成第一次对话分析");
        achievement.setUnlocked(true);
        achievementList = Arrays.asList(achievement);

        PersonalityCardDTO card = new PersonalityCardDTO();
        card.setId(1L);
        card.setName("洞察者");
        card.setRarity(1);
        card.setEmoji("🔍");
        card.setDescription("善于洞察人心");
        cardList = Arrays.asList(card);

        UserCardDTO userCard = new UserCardDTO();
        userCard.setId(1L);
        userCard.setCardId(1L);
        userCard.setCardName("洞察者");
        userCard.setRarity(1);
        userCard.setEmoji("🔍");
        userCard.setQuantity(1);
        userCardList = Arrays.asList(userCard);
    }

    @Test
    void getAllAchievements_Success() throws Exception {
        when(gameService.getAllAchievements(1L)).thenReturn(achievementList);

        mockMvc.perform(get("/api/achievements/list")
                .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].name").value("首次分析"))
                .andExpect(jsonPath("$.data[0].unlocked").value(true));
    }

    @Test
    void getUserAchievements_Success() throws Exception {
        when(gameService.getUserAchievements(1L)).thenReturn(achievementList);

        mockMvc.perform(get("/api/achievements/user")
                .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].name").value("首次分析"));
    }

    @Test
    void getAllCardDefinitions_Success() throws Exception {
        when(gameService.getAllCardDefinitions()).thenReturn(cardList);

        mockMvc.perform(get("/api/cards/definitions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].name").value("洞察者"))
                .andExpect(jsonPath("$.data[0].rarity").value(1));
    }

    @Test
    void getUserCards_Success() throws Exception {
        when(gameService.getUserCards(1L)).thenReturn(userCardList);

        mockMvc.perform(get("/api/cards/user")
                .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].cardName").value("洞察者"))
                .andExpect(jsonPath("$.data[0].quantity").value(1));
    }
}
