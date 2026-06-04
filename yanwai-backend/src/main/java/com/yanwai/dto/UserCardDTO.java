package com.yanwai.dto;

import lombok.Data;

@Data
public class UserCardDTO {
    private Long id;
    private Long cardId;
    private String name;
    private Integer rarity;
    private String emoji;
    private String description;
    private Integer quantity;
    private Boolean isNew;
}
