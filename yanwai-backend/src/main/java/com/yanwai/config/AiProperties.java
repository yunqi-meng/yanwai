package com.yanwai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "yanwai.ai")
public class AiProperties {
    private String apiKey;
    private String baseUrl;
    private String model;
    private String visionBaseUrl;
    private String visionApiKey;
    private String visionModel;
    private String visionUrl;
    private Integer maxTokens = 2000;
    private Double temperature = 0.7;
    private Boolean enableMock = false;
}
