package com.yanwai.dto;

import lombok.Data;

@Data
public class PersonalityCardDTO {
    private Long id;
    private String name;
    private Integer rarity;
    private String emoji;
    private String description;
    private String personalityType;
    private String trait;
}
