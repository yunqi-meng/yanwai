package com.yanwai.controller;

import com.yanwai.dto.AchievementDTO;
import com.yanwai.dto.PersonalityCardDTO;
import com.yanwai.dto.Result;
import com.yanwai.dto.UserCardDTO;
import com.yanwai.service.GameService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/achievements/list")
    public Result<List<AchievementDTO>> getAllAchievements(@RequestHeader("X-User-Id") Long userId) {
        List<AchievementDTO> achievements = gameService.getAllAchievements(userId);
        return Result.success(achievements);
    }

    @GetMapping("/achievements/user")
    public Result<List<AchievementDTO>> getUserAchievements(@RequestHeader("X-User-Id") Long userId) {
        List<AchievementDTO> achievements = gameService.getUserAchievements(userId);
        return Result.success(achievements);
    }

    @GetMapping("/cards/definitions")
    public Result<List<PersonalityCardDTO>> getAllCardDefinitions() {
        List<PersonalityCardDTO> cards = gameService.getAllCardDefinitions();
        return Result.success(cards);
    }

    @GetMapping("/cards/user")
    public Result<List<UserCardDTO>> getUserCards(@RequestHeader("X-User-Id") Long userId) {
        List<UserCardDTO> cards = gameService.getUserCards(userId);
        return Result.success(cards);
    }

}
