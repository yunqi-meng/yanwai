package com.yanwai.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AchievementDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String conditionField;
    private Integer conditionValue;
    private String rewardType;
    private String rewardValue;
    private String icon;
    private Boolean unlocked;
    private LocalDateTime unlockedAt;
}
