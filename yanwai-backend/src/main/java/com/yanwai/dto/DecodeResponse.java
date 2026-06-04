package com.yanwai.dto;

import lombok.Data;
import java.util.List;

@Data
public class DecodeResponse {
    private AnalysisResultDTO analysis;
    private CardDropResultDTO newCard;
    private List<AchievementDTO> newAchievements;
    private String originalImage;
}
