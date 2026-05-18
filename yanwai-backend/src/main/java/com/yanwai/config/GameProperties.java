package com.yanwai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "yanwai.game")
public class GameProperties {
    private Double cardDropProbability = 0.3;
    private Integer fragmentCountForSynthesis = 5;
    private Integer freeDailyAnalysis = 3;
}
