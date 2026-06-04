package com.yanwai.dto;

import lombok.Data;

@Data
public class CardDropResultDTO {
    private Long cardId;
    private String name;
    private Integer rarity;
    private String emoji;
    private String description;
    private Boolean isNew;
}
