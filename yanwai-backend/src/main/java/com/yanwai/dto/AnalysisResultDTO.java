package com.yanwai.dto;

import lombok.Data;
import java.util.List;

@Data
public class AnalysisResultDTO {
    private String relationship;
    private List<EmotionPoint> emotionCurve;
    private List<TranslationItem> translations;
    private List<String> advice;
    private PersonalityCardDTO personalityCard;

    @Data
    public static class EmotionPoint {
        private Integer index;
        private String speaker;
        private Double emotionScore;
        private String emotionType;
        private String description;
    }

    @Data
    public static class TranslationItem {
        private Integer index;
        private String original;
        private String literal;
        private String subtext;
        private Double emotionScore;
    }
}
